package com.pawsmedic.features.products.data.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductDtoTest {
    private val json = Json { encodeDefaults = false }

    @Test
    fun decodesGeneratedIntIdAndEnglishColumnNames() {
        val product = json.decodeFromString<ProductDto>(
            """
            {
              "id": 1,
              "tenant_id": "tenant-1",
              "sku": "FOOD-1",
              "name": "Food",
              "description": "Dry food",
              "price_cents": 1250,
              "stock_quantity": 4,
              "active": true
            }
            """.trimIndent()
        )

        assertEquals(1, product.id)
        assertEquals("Food", product.name)
        assertEquals(1250, product.priceCents)
        assertTrue(product.active)
    }

    @Test
    fun insertPayloadDoesNotContainGeneratedId() {
        val payload = ProductDto.fromDomain(
            ProductDto(
                id = 42,
                tenantId = "tenant-1",
                name = "Food",
                priceCents = 1250,
                stockQuantity = 4,
                active = false
            ).toDomain(),
            "tenant-1"
        )

        val encoded = json.encodeToString(payload)

        assertFalse(encoded.contains("\"id\""))
        assertTrue(encoded.contains("\"tenant_id\""))
        assertTrue(encoded.contains("\"price_cents\""))
        assertTrue(encoded.contains("\"stock_quantity\""))
        assertTrue(encoded.contains("\"active\""))
    }
}
