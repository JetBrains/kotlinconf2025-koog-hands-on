package ai.koog.kooging.book.model

fun main() {
    println("Starting WebShop application...")
    
    val webShop = WebShop()
    
    // Print all products to verify loading
    val allProducts = webShop.getAllProducts()
    println("Total products loaded: ${allProducts.size}")
    
    if (allProducts.isNotEmpty()) {
        println("\nFirst 5 products:")
        allProducts.take(5).forEach { product ->
            println("ID: ${product.id}, Name: ${product.name}, Price: $${product.price}")
        }
        
        // Test search functionality
        val searchTerm = "tomato"
        println("\nSearching for '$searchTerm':")
        val searchResults = webShop.searchProducts(searchTerm)
        searchResults.forEach { product ->
            println("ID: ${product.id}, Name: ${product.name}, Price: $${product.price}")
        }
        
        // Test basket functionality
        println("\nTesting basket functionality:")
        webShop.addToBasket(1) // Add Lettuce
        webShop.addToBasket(2) // Add Tomatoes
        webShop.addToBasket(8) // Add Olive Oil
        
        println("Basket contents:")
        webShop.getBasketContent().forEach { product ->
            println("ID: ${product.id}, Name: ${product.name}, Price: $${product.price}")
        }
    } else {
        println("No products were loaded from the JSON file.")
    }
}