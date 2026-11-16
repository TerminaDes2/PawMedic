package com.pawsmedic.features.products.data.datasource

import com.pawsmedic.features.products.data.dto.ProductDto
import com.pawsmedic.features.products.data.dto.ProductTypeDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupabaseProductDataSource(
    private val supabase: SupabaseClient
) : ProductDataSource {
    override suspend fun getProductsByTenant(tenantId: String): List<ProductDto> =
        supabase.postgrest["products"].select {
            filter {
                eq("tenant_id", tenantId)
                eq("active", true)
                eq("published", true)
            }
        }.decodeList<ProductDto>()
            .let { products ->
                val types = getProductTypes().associateBy { it.id }
                products.map { product ->
                    product.copy(productType = types[product.typeId])
                }
            }

    override suspend fun getProductTypes(): List<ProductTypeDto> =
        supabase.postgrest["product_types"].select { }.decodeList<ProductTypeDto>()

    override suspend fun addProduct(product: ProductDto): ProductDto =
        supabase.postgrest["products"]
            .insert(product) {
                select()
            }
            .decodeSingle<ProductDto>()
}
