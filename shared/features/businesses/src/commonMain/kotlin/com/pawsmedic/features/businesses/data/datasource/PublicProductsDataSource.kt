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
    @SerialName("image_url") val imageUrl: String,
    @SerialName("category_name") val typeName: String? = null
)

@Serializable
private data class DirectPublicProduct(
    val id: Int,
    val name: String,
    val description: String,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("type_id") val typeId: Int?
)

@Serializable
private data class PublicProductType(
    val id: Int,
    val name: String
)

@Serializable
private data class PublicTenantStatus(
    val status: String
)

class PublicProductsDataSource(private val supabase: SupabaseClient) {
    suspend fun list(tenantId: String): List<PublicProduct> {
        val rpcProducts = supabase.postgrest.rpc("get_public_products", buildJsonObject {
            put("p_tenant_id", tenantId)
        }).decodeList<PublicProduct>()

        if (rpcProducts.isEmpty() || rpcProducts.any { it.typeName != null }) {
            return rpcProducts
        }

        val activeTenants = supabase.postgrest["tenants"].select {
            filter {
                eq("id", tenantId)
                eq("status", "ACTIVE")
            }
        }.decodeList<PublicTenantStatus>()
        if (activeTenants.isEmpty()) return emptyList()

        val directProducts = supabase.postgrest["products"].select {
            filter {
                eq("tenant_id", tenantId)
                eq("active", true)
                eq("published", true)
            }
        }.decodeList<DirectPublicProduct>()

        val productTypes = supabase.postgrest["product_types"]
            .select()
            .decodeList<PublicProductType>()
            .associateBy { it.id }

        return directProducts.map { product ->
            PublicProduct(
                id = product.id.toString(),
                name = product.name,
                description = product.description,
                priceCents = product.priceCents,
                imageUrl = product.imageUrl,
                typeName = product.typeId?.let(productTypes::get)?.name
            )
        }
    }
}
