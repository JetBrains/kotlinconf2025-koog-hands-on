package ai.koog.kooging.book.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ProductCatalog(val products: List<Product>)

/**
 * A class that manages an online shop with product catalog and shopping basket functionality.
 */
class WebShop {
    private val catalogue = mutableListOf<Product>()
    private val basket = mutableListOf<Product>()

    init {
        loadProductsFromJson()
    }

    /**
     * Loads product data from a JSON file into the catalogue.
     */
    private fun loadProductsFromJson() {
        try {
            val jsonFile = WebShop::class.java.classLoader.getResource("products.json")
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

    /**
     * Adds a product to the shopping basket by its ID.
     * 
     * @param id The ID of the product to add
     */
    fun addToBasket(id: Int) {
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
     * Searches for products in the catalogue by name.
     * 
     * @param searchTerm The term to search for in product names
     * @return A list of products matching the search term
     */
    fun searchProducts(searchTerm: String): List<Product> {
        return catalogue.filter { it.name.contains(searchTerm, ignoreCase = true) }
    }

    /**
     * Gets all products available in the catalogue.
     * 
     * @return A list of all products
     */
    fun getAllProducts(): List<Product> {
        return catalogue.toList()
    }
}
