package com.pawsmedic.desktop.veterinary.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CatalogImportTest {
    @Test
    fun parsesMultipleProductsAndDecimalPrices() {
        val items = parseCatalogImport("\uFEFFnombre: Alimento\nsku: A1\nprecio: 12,50\nexistencias: 2\n---\nnombre: Juguete\nsku: B2\nprecio: 5\nexistencias: 0", ImportKind.PRODUCT)
        assertEquals(2, items.size)
        assertEquals(1250, items[0].priceCents)
        assertEquals("B2", items[1].sku)
    }

    @Test
    fun validatesBeforeImport() {
        assertFailsWith<IllegalArgumentException> { parseCatalogImport("nombre: A\nprecio: 2.001\nduracion: 5", ImportKind.SERVICE) }
        assertFailsWith<IllegalArgumentException> { parseCatalogImport("nombre: A\nprecio: 2\nduracion: 0", ImportKind.SERVICE) }
        assertFailsWith<IllegalArgumentException> { parseCatalogImport("nombre: A\ncategoria: comida", ImportKind.PRODUCT) }
        assertFailsWith<IllegalArgumentException> { parseCatalogImport("", ImportKind.PRODUCT) }
        assertFailsWith<IllegalArgumentException> {
            parseCatalogImport("nombre: A\nsku: X\nprecio: 2\nexistencias: 1\n---\nnombre: B\nsku: X\nprecio: 3\nexistencias: 1", ImportKind.PRODUCT)
        }
    }
}
