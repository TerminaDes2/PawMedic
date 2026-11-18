import { classifyFcmResponse, notificationData } from "../_shared/notification-fcm.mjs";

type Delivery = {
  delivery_id: string;
  lease_id: string;
  device_token: string;
  message_type: string;
  expires_at: string | null;
};

function required(name: string): string {
  const value = Deno.env.get(name);
  if (!value) throw new Error(`Missing configuration: ${name}`);
  return value;
}

function base64Url(input: Uint8Array): string {
  return btoa(String.fromCharCode(...input)).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", "");
}

function encodeJson(input: unknown): string {
  return base64Url(new TextEncoder().encode(JSON.stringify(input)));
}

async function authorized(actual: string, expected: string): Promise<boolean> {
  const encode = (value: string) => new TextEncoder().encode(value);
  const left = new Uint8Array(await crypto.subtle.digest("SHA-256", encode(actual)));
  const right = new Uint8Array(await crypto.subtle.digest("SHA-256", encode(expected)));
  let difference = 0;
  for (let i = 0; i < left.length; i++) difference |= left[i] ^ right[i];
  return difference === 0;
}

async function firebaseAccessToken(account: { client_email: string; private_key: string }): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const header = encodeJson({ alg: "RS256", typ: "JWT" });
  const payload = encodeJson({
    iss: account.client_email,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    iat: now,
    exp: now + 3600,
  });
  const keyBytes = Uint8Array.from(
    atob(account.private_key.replace(/-----[^-]+-----|\s/g, "")),
    (character) => character.charCodeAt(0),
  );
  const key = await crypto.subtle.importKey("pkcs8", keyBytes, { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["sign"]);
  const unsigned = `${header}.${payload}`;
  const signature = new Uint8Array(await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, new TextEncoder().encode(unsigned)));
  const response = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: `${unsigned}.${base64Url(signature)}`,
    }),
    signal: AbortSignal.timeout(10000),
  });
  if (!response.ok) throw new Error(`Firebase authorization failed (${response.status})`);
  const result = await response.json();
  if (typeof result.access_token !== "string") throw new Error("Firebase authorization response invalid");
  return result.access_token;
}

async function rpc(name: string, params: Record<string, unknown> = {}) {
  const key = required("SUPABASE_SERVICE_ROLE_KEY");
  const response = await fetch(`${required("SUPABASE_URL")}/rest/v1/rpc/${name}`, {
    method: "POST",
    headers: { apikey: key, Authorization: `Bearer ${key}`, "Content-Type": "application/json" },
    body: JSON.stringify(params),
    signal: AbortSignal.timeout(10000),
  });
  if (!response.ok) throw new Error(`Notification database request failed (${response.status})`);
  return response.status === 204 ? null : await response.json();
}

export async function handleNotificationDispatch(request: Request) {
  if (request.method !== "POST") return new Response("Method not allowed", { status: 405 });
  try {
    const secret = required("NOTIFICATION_DISPATCH_SECRET");
    if (secret.length < 32) throw new Error("Dispatcher secret must contain at least 32 characters");
    if (!await authorized(request.headers.get("x-dispatch-secret") ?? "", secret)) {
      return new Response("Unauthorized", { status: 401 });
    }
    const account = JSON.parse(required("FIREBASE_SERVICE_ACCOUNT"));
    if (typeof account.client_email !== "string" || typeof account.private_key !== "string" ||
        typeof account.project_id !== "string" || !/^[a-z0-9-]+$/.test(account.project_id)) {
      throw new Error("Firebase service account configuration invalid");
    }
    await rpc("prepare_appointment_notification_deliveries", { p_limit: 100 });
    const deliveries = await rpc("claim_appointment_notification_deliveries", { p_limit: 10 }) as Delivery[];
    const accessToken = deliveries.length > 0 ? await firebaseAccessToken(account) : null;
    const outcomes = await Promise.all(deliveries.map(async (delivery) => {
      let result = "retry";
      try {
        const remaining = delivery.expires_at === null
          ? 3600
          : Math.floor((Date.parse(delivery.expires_at) - Date.now()) / 1000);
        if (!Number.isFinite(remaining)) throw new Error("Invalid notification expiration");
        if (remaining <= 0) {
          await rpc("finish_appointment_notification_delivery", {
            p_delivery_id: delivery.delivery_id,
            p_lease_id: delivery.lease_id,
            p_result: "permanent_failure",
          });
          return "permanent_failure";
        }
        const response = await fetch(`https://fcm.googleapis.com/v1/projects/${account.project_id}/messages:send`, {
          method: "POST",
          headers: { Authorization: `Bearer ${accessToken}`, "Content-Type": "application/json" },
          body: JSON.stringify({ message: {
            token: delivery.device_token,
            data: notificationData(delivery.message_type, delivery.delivery_id),
            android: { priority: "high", ttl: `${Math.min(3600, remaining)}s` },
          } }),
          signal: AbortSignal.timeout(10000),
        });
        const body = await response.json().catch(() => null);
        result = classifyFcmResponse(response.status, body);
      } catch {
        result = "retry";
      }
      await rpc("finish_appointment_notification_delivery", {
        p_delivery_id: delivery.delivery_id,
        p_lease_id: delivery.lease_id,
        p_result: result,
      });
      return result;
    }));
    const counts = {
      processed: outcomes.length,
      sent: outcomes.filter((outcome) => outcome === "sent").length,
      retry: outcomes.filter((outcome) => outcome === "retry").length,
      invalidToken: outcomes.filter((outcome) => outcome === "invalid_token").length,
      permanentFailure: outcomes.filter((outcome) => outcome === "permanent_failure").length,
    };
    if (counts.retry || counts.permanentFailure) {
      console.warn("Notification deliveries require attention", counts);
    }
    return Response.json(counts);
  } catch (error) {
    console.error("Notification dispatcher failed", error instanceof Error ? error.name : "unknown error");
    return new Response("Notification dispatcher failed", { status: 500 });
  }
}
