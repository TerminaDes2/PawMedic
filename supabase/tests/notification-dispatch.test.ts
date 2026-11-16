import { handleNotificationDispatch } from "../functions/appointment-notifications/handler.ts";

const secret = "local-test-secret-only-0123456789";

Deno.test("dispatcher rejects unauthenticated requests before contacting services", async () => {
  Deno.env.set("NOTIFICATION_DISPATCH_SECRET", secret);
  const originalFetch = globalThis.fetch;
  globalThis.fetch = () => { throw new Error("unexpected network request"); };
  try {
    const response = await handleNotificationDispatch(new Request("https://test.invalid", { method: "POST" }));
    if (response.status !== 401) throw new Error(`expected 401, got ${response.status}`);
  } finally {
    globalThis.fetch = originalFetch;
    Deno.env.delete("NOTIFICATION_DISPATCH_SECRET");
  }
});

Deno.test("dispatcher does not request Firebase authorization when there are no deliveries", async () => {
  Deno.env.set("NOTIFICATION_DISPATCH_SECRET", secret);
  Deno.env.set("SUPABASE_URL", "https://test.invalid");
  Deno.env.set("SUPABASE_SERVICE_ROLE_KEY", "local-test-only");
  Deno.env.set("FIREBASE_SERVICE_ACCOUNT", JSON.stringify({
    client_email: "test@example.invalid", private_key: "unused-test-key", project_id: "example-project",
  }));
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async (input) => {
    const url = String(input);
    if (url.endsWith("/prepare_appointment_notification_deliveries")) return Response.json(0);
    if (url.endsWith("/claim_appointment_notification_deliveries")) return Response.json([]);
    throw new Error("unexpected network request");
  };
  try {
    const response = await handleNotificationDispatch(new Request("https://test.invalid", {
      method: "POST", headers: { "x-dispatch-secret": secret },
    }));
    if (response.status !== 200 || (await response.json()).processed !== 0) {
      throw new Error("empty queue was not handled");
    }
  } finally {
    globalThis.fetch = originalFetch;
    for (const name of ["NOTIFICATION_DISPATCH_SECRET", "SUPABASE_URL", "SUPABASE_SERVICE_ROLE_KEY", "FIREBASE_SERVICE_ACCOUNT"]) {
      Deno.env.delete(name);
    }
  }
});

Deno.test("dispatcher sends only generic data and acknowledges a delivery", async () => {
  const keyPair = await crypto.subtle.generateKey(
    { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256" },
    true,
    ["sign", "verify"],
  );
  const pkcs8 = new Uint8Array(await crypto.subtle.exportKey("pkcs8", keyPair.privateKey));
  const pem = `-----BEGIN PRIVATE KEY-----\n${btoa(String.fromCharCode(...pkcs8))}\n-----END PRIVATE KEY-----`;
  Deno.env.set("NOTIFICATION_DISPATCH_SECRET", secret);
  Deno.env.set("SUPABASE_URL", "https://test.invalid");
  Deno.env.set("SUPABASE_SERVICE_ROLE_KEY", "local-test-only");
  Deno.env.set("FIREBASE_SERVICE_ACCOUNT", JSON.stringify({
    client_email: "test@example.invalid", private_key: pem, project_id: "example-project",
  }));
  const deliveryId = "b2640df6-9a42-4bb0-8d9c-9c038f6bd704";
  let deliveredPayload: Record<string, unknown> | undefined;
  let finishParams: Record<string, unknown> | undefined;
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async (input, init) => {
    const url = String(input);
    if (url === "https://oauth2.googleapis.com/token") return Response.json({ access_token: "local-test-token" });
    if (url.endsWith("/prepare_appointment_notification_deliveries")) return Response.json(1);
    if (url.endsWith("/claim_appointment_notification_deliveries")) return Response.json([{
      delivery_id: deliveryId,
      lease_id: "local-test-lease",
      device_token: "local-test-device",
      message_type: "appointment_reminder_1h",
      expires_at: new Date(Date.now() + 1800_000).toISOString(),
    }]);
    if (url.includes("fcm.googleapis.com")) {
      deliveredPayload = JSON.parse(String(init?.body));
      return Response.json({ name: "local-test-message" });
    }
    if (url.endsWith("/finish_appointment_notification_delivery")) {
      finishParams = JSON.parse(String(init?.body));
      return new Response(null, { status: 204 });
    }
    throw new Error("unexpected network request");
  };
  try {
    const response = await handleNotificationDispatch(new Request("https://test.invalid", {
      method: "POST", headers: { "x-dispatch-secret": secret },
    }));
    if (response.status !== 200) throw new Error(`expected 200, got ${response.status}`);
    const message = deliveredPayload?.message as { data?: Record<string, string> } | undefined;
    if (JSON.stringify(message?.data) !== JSON.stringify({ type: "appointment_reminder_1h", delivery_id: deliveryId })) {
      throw new Error("incorrect notification data");
    }
    if (finishParams?.p_result !== "sent" || finishParams?.p_lease_id !== "local-test-lease") {
      throw new Error("delivery was not acknowledged");
    }
  } finally {
    globalThis.fetch = originalFetch;
    for (const name of ["NOTIFICATION_DISPATCH_SECRET", "SUPABASE_URL", "SUPABASE_SERVICE_ROLE_KEY", "FIREBASE_SERVICE_ACCOUNT"]) {
      Deno.env.delete(name);
    }
  }
});
