import { json, supabaseForRequest } from "../_shared/auth.ts";

Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
  try {
    const client = supabaseForRequest(request);
    const { data: userData } = await client.auth.getUser();
    if (!userData.user) return json({ error: "Unauthorized" }, 401);
    const body = await request.json();
    if (typeof body.entity_type !== "string" || typeof body.entity_id !== "string" ||
        typeof body.status !== "string") return json({ error: "Invalid status change" }, 400);
    const { data, error } = await client.rpc("change_entity_status", {
      p_entity_type: body.entity_type, p_entity_id: body.entity_id,
      p_status: body.status, p_actor_id: userData.user.id,
    });
    if (error) return json({ error: error.message }, 400);
    return json({ result: data });
  } catch (error) {
    if (error instanceof Response) return error;
    return json({ error: "Unexpected error" }, 500);
  }
});
