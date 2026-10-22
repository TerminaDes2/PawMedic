package com.pawsmedic.features.products.data.datasource

import com.pawsmedic.features.products.data.dto.ProductDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseProductDataSource(
    private val supabase: SupabaseClient
) : ProductDataSource {
    override suspend fun getProductsByTenant(tenantId: String): List<ProductDto> =
        supabase.postgrest["products"]
            .select {
                filter {
                    eq("tenant_id", tenantId)
                    eq("activo", true)
                }
            }
            .decodeList<ProductDto>()

    override suspend fun addProduct(product: ProductDto): ProductDto =
        supabase.postgrest["products"]
            .insert(product) {
                select()
            }
            .decodeSingle<ProductDto>()
}
