import { json, supabaseForRequest } from "../_shared/auth.ts";

// The RPC must lock overlapping appointments and availability rows before
// creating a booking, making retries safe with the supplied idempotency key.
Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
  try {
    const client = supabaseForRequest(request);
    const { data: userData, error: userError } = await client.auth.getUser();
    if (userError || !userData.user) return json({ error: "Unauthorized" }, 401);
    const body = await request.json();
    for (const key of ["tenant_id", "pet_id", "service_id", "starts_at", "ends_at", "idempotency_key"]) {
      if (typeof body[key] !== "string" || body[key].trim() === "") return json({ error: `Missing field: ${key}` }, 400);
    }
    const { data, error } = await client.rpc("create_critical_booking", {
      p_tenant_id: body.tenant_id, p_pet_id: body.pet_id, p_service_id: body.service_id,
      p_booked_by: userData.user.id, p_starts_at: body.starts_at, p_ends_at: body.ends_at,
      p_idempotency_key: body.idempotency_key,
    });
    if (error) return json({ error: error.message }, error.code === "23505" ? 409 : 400);
    return json({ appointment: data }, 201);
  } catch (error) {
    if (error instanceof Response) return error;
    return json({ error: "Unexpected error" }, 500);
  }
});
