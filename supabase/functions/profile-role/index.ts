import { json, supabaseForRequest } from "../_shared/auth.ts";

Deno.serve(async (request) => {
  if (request.method !== "GET") return json({ error: "Method not allowed" }, 405);
  try {
    const client = supabaseForRequest(request);
    const { data: userData, error: userError } = await client.auth.getUser();
    if (userError || !userData.user) return json({ error: "Unauthorized" }, 401);

    const { data, error } = await client
      .from("profiles")
      .select("id, email, display_name, role, tenant_id")
      .eq("id", userData.user.id)
      .single();
    if (error) return json({ error: "Profile unavailable" }, 404);
    return json({ profile: data });
  } catch (error) {
    if (error instanceof Response) return error;
    return json({ error: "Unexpected error" }, 500);
  }
});
