package ai.koog.kooging.book.app.service

import ai.koog.kooging.book.app.model.Product
import ai.koog.kooging.book.app.model.ProductCatalog
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * A class that manages an online shop with product catalog and shopping basket functionality.
 */
class WebShopService private constructor() {

    companion object {

        private val logger = LoggerFactory.getLogger(WebShopService::class.java)

        val instance: WebShopService by lazy { WebShopService() }
    }

    private val catalogue = mutableListOf<Product>()
    private val basket = mutableListOf<Product>()

    init {
        loadProductsFromJson()
    }

    /**
     * Adds a product to the shopping basket by its ID.
     *
     * @param id The ID of the product to add
     */
    fun putToBasket(id: Int) {
        findProduct(id)?.let { basket.add(it) }
    }

    /**
     * Removes a product from the shopping basket by its ID.
     *
     * @param id The ID of the product to remove
     */
    fun removeFromBasket(id: Int) {
        basket.removeIf { it.id == id }
    }

    /**
     * Finds a product in the catalogue by its ID.
     *
     * @param id The ID of the product to find
     * @return The product if found, null otherwise
     */
    fun findProduct(id: Int): Product? {
        return catalogue.firstOrNull { it.id == id }
    }

    /**
     * Empties the shopping basket by removing all products.
     */
    fun emptyBasket() {
        basket.clear()
    }

    /**
     * Gets the current contents of the shopping basket.
     *
     * @return A list of products in the basket
     */
    fun getBasketContent(): List<Product> {
        return basket.toList()
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     *
     * @param query The first string to compare.
     * @param productName The second string to compare.
     * @return The Levenshtein distance between the two strings.
     */
    private fun levenshteinDistance(query: String, productName: String): Int {
        val m = query.length
        val n = productName.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) {
            dp[i][0] = i
        }
        for (j in 0..n) {
            dp[0][j] = j
        }

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (query[i - 1] == productName[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    minOf(
                        dp[i - 1][j] + 1,     // deletion
                        dp[i][j - 1] + 1,     // insertion
                        dp[i - 1][j - 1] + 1  // substitution
                    )
                }
            }
        }

        return dp[m][n]
    }

    /**
     * Searches for products in the catalogue by name.
     *
     * @param query The term to search for in product names
     * @return A list of products matching the search term
     */
    fun searchProducts(query: String): List<Product> {
        return catalogue.filter { it.name.contains(query, ignoreCase = true) || levenshteinDistance(query, it.name) <= 5 }
    }

    /**
     * Gets all products available in the catalogue.
     *
     * @return A list of all products
     */
    fun getAllProducts(): List<Product> {
        return catalogue.toList()
    }

    //region Private Methods

    /**
     * Loads product data from a JSON file into the catalogue.
     */
    private fun loadProductsFromJson() {
        try {
            val jsonFile = WebShopService::class.java.classLoader.getResource("products.json")
            if (jsonFile != null) {
                val jsonContent = jsonFile.readText()
                val productCatalog = Json.decodeFromString<ProductCatalog>(jsonContent)
                catalogue.addAll(productCatalog.products)
                println("Loaded ${catalogue.size} products from JSON")
            } else {
                println("Products JSON file not found")
            }
        } catch (e: Exception) {
            println("Error loading products from JSON: ${e.message}")
        }
    }

    //endregion Private Methods
}