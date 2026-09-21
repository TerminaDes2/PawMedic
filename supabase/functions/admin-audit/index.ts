import { json, supabaseForRequest } from "../_shared/auth.ts";

Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
  try {
    const client = supabaseForRequest(request);
    const { data: userData, error: userError } = await client.auth.getUser();
    if (userError || !userData.user) return json({ error: "Unauthorized" }, 401);
    const body = await request.json();
    if (typeof body.action !== "string" || typeof body.entity_type !== "string") {
      return json({ error: "action and entity_type are required" }, 400);
    }
    const { error } = await client.from("audit_logs").insert({
      actor_id: userData.user.id, tenant_id: body.tenant_id ?? null,
      action: body.action, entity_type: body.entity_type,
      entity_id: body.entity_id ?? null, metadata: body.metadata ?? {},
    });
    if (error) return json({ error: error.message }, 400);
    return json({ accepted: true }, 201);
  } catch (error) {
    if (error instanceof Response) return error;
    return json({ error: "Unexpected error" }, 500);
  }
});
