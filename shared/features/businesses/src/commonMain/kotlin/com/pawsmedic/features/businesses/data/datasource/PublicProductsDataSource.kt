package com.pawsmedic.features.businesses.data.datasource

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class PublicProduct(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("image_url") val imageUrl: String
)

class PublicProductsDataSource(private val supabase: SupabaseClient) {
    suspend fun list(tenantId: String): List<PublicProduct> =
        supabase.postgrest.rpc("get_public_products", buildJsonObject {
            put("p_tenant_id", tenantId)
        }).decodeList<PublicProduct>()
}
