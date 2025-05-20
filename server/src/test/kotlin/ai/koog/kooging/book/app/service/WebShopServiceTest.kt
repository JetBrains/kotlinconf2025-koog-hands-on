package ai.koog.kooging.book.app.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebShopServiceTest {

    @Test
    fun testProductLoading() {
        val webShop = WebShopService()

        val allProducts = webShop.getAllProducts()
        assertTrue(allProducts.isNotEmpty(), "Product catalogue should not be empty")

        if (allProducts.size >= 5) {
            val firstFiveProducts = allProducts.take(5)
            assertEquals(5, firstFiveProducts.size, "Should retrieve 5 products")

            firstFiveProducts.forEach { product ->
                assertTrue(product.id > 0, "Product ID should be positive")
                assertTrue(product.name.isNotEmpty(), "Product name should not be empty")
                assertTrue(product.price >= 0, "Product price should be non-negative")
            }
        }
    }

    @Test
    fun testSearchFunctionality() {
        val webShop = WebShopService()

        val searchTerm = "tomato"
        val searchResults = webShop.searchProducts(searchTerm = searchTerm)
        
        assertTrue(
            searchResults.isNotEmpty(),
            "Search for '$searchTerm' should return results"
        )

        searchResults.forEach { product ->
            assertTrue(
                product.name.contains(searchTerm, ignoreCase = true),
                "Search result '${product.name}' should contain '$searchTerm'"
            )
        }
    }

    @Test
    fun testBasketInitialState() {
        val webShop = WebShopService()

        assertTrue(
            webShop.getBasketContent().isEmpty(),
            "Basket should be empty initially"
        )
    }

        @Test
    fun testBasketFunctionality() {
        val webShop = WebShopService()
        assertTrue(webShop.getBasketContent().isEmpty())

        webShop.addToBasket(1)
        webShop.addToBasket(2)
        webShop.addToBasket(8)

        val basketContents = webShop.getBasketContent()
        assertEquals(3, basketContents.size, "Basket should contain 3 items")

        val productIds = basketContents.map { it.id }
        assertTrue(productIds.contains(1), "Basket should contain product with ID 1")
        assertTrue(productIds.contains(2), "Basket should contain product with ID 2")
        assertTrue(productIds.contains(8), "Basket should contain product with ID 8")
    }
    
    @Test
    fun testRemoveFromBasket() {
        val webShop = WebShopService()

        webShop.addToBasket(1)
        webShop.addToBasket(2)

        webShop.removeFromBasket(1)

        val basketContents = webShop.getBasketContent()
        assertEquals(1, basketContents.size, "Basket should contain 1 item after removal")
        assertEquals(2, basketContents[0].id, "Remaining item should have ID 2")
    }
    
    @Test
    fun testEmptyBasket() {
        val webShop = WebShopService()
        webShop.addToBasket(1)

        webShop.emptyBasket()

        assertEquals(0, webShop.getBasketContent().size, "Basket should be empty after emptying")
    }
    
    @Test
    fun testFindProduct() {
        val webShop = WebShopService()
        val product = webShop.findProduct(1)

        assertTrue(product != null, "Should find product with ID 1")
        assertEquals(1, product.id, "Product should have ID 1")
        assertTrue(product.name.isNotEmpty(), "Product should have a name")
    }

    @Test
    fun testFindNonExistingProduct() {
        val webShop = WebShopService()
        val nonExistentProduct = webShop.findProduct(9999)
        assertEquals(null, nonExistentProduct, "Should return null for non-existent product")
    }
}