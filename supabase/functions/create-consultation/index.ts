import { json, supabaseForRequest } from "../_shared/auth.ts";

const requiredText = ["tenant_id", "pet_id", "reason", "observations", "idempotency_key"];

Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);

  try {
    const client = supabaseForRequest(request);
    const body = await request.json();
    for (const field of requiredText) {
      if (typeof body[field] !== "string" || body[field].trim() === "") {
        return json({ error: `Missing field: ${field}` }, 400);
      }
    }

    const { data: userData, error: userError } = await client.auth.getUser();
    if (userError || !userData.user) return json({ error: "Unauthorized" }, 401);

    const { data: profile, error: profileError } = await client
      .from("profiles")
      .select("role")
      .eq("id", userData.user.id)
      .single();
    if (profileError || profile.role !== "VETERINARY_BUSINESS") {
      return json({ error: "Forbidden" }, 403);
    }

    const { data, error } = await client
      .from("clinical_consultations")
      .insert({
        pet_id: body.pet_id,
        tenant_id: body.tenant_id,
        veterinarian_id: userData.user.id,
        reason: body.reason.trim(),
        observations: body.observations.trim(),
        diagnosis: body.diagnosis ?? "",
        plan: body.plan ?? "",
        weight_grams: body.weight_grams ?? null,
        occurred_at: body.occurred_at ?? new Date().toISOString(),
        status: "PUBLISHED",
        idempotency_key: body.idempotency_key,
      })
      .select()
      .single();
    if (error) return json({ error: error.message }, error.code === "23505" ? 409 : 400);

    return json({ consultation: data }, 201);
  } catch (error) {
    if (error instanceof Response) return error;
    return json({ error: "Unexpected error" }, 500);
  }
});
