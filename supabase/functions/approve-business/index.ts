import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { json, supabaseForRequest } from "../_shared/auth.ts";

// Approval and tenant creation run in one database RPC transaction. The
// service-role key is read only from Edge Function secrets, never client code.
Deno.serve(async (request) => {
  if (request.method !== "POST") return json({ error: "Method not allowed" }, 405);
  try {
    const caller = supabaseForRequest(request);
    const { data: userData, error: userError } = await caller.auth.getUser();
    if (userError || !userData.user) return json({ error: "Unauthorized" }, 401);

    const { data: profile } = await caller.from("profiles").select("role")
      .eq("id", userData.user.id).single();
    if (profile?.role !== "SUPERADMIN") return json({ error: "Forbidden" }, 403);

    const { business_id: businessId } = await request.json();
    if (typeof businessId !== "string") return json({ error: "business_id is required" }, 400);
    const url = Deno.env.get("SUPABASE_URL");
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    if (!url || !serviceKey) return json({ error: "Function configuration error" }, 500);
    const admin = createClient(url, serviceKey);
    const { data, error } = await admin.rpc("approve_business_create_tenant", {
      p_business_id: businessId, p_actor_id: userData.user.id,
    });
    if (error) return json({ error: error.message }, 400);
    return json({ business: data });
  } catch (error) {
    if (error instanceof Response) return error;
    return json({ error: "Unexpected error" }, 500);
  }
});
