package ai.koog.book.agent

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.book.app.model.Product
import ai.koog.book.app.service.WebShopService

/**
 * Set of tools for the Cooking Agent
 */
@LLMDescription("Tools for web shop cooking agent")
class CookingAgentTools : ToolSet {

    //region Task 2

    @Tool
    @LLMDescription("Search for an product by ingredient name in the web shop")
    fun searchProduct(
        @LLMDescription("Ingredient name") ingredientName: String,
    ): List<Product> {
        return WebShopService.instance.searchProductsInCatalogue(ingredientName)
    }

    /**
     * Adds a specific product to the shopping cart using its ID.
     * This tool allows the LLM to add products to the user's shopping cart.
     *
     * @return A success/failure message indicating if the product was added to the cart
     */
    @Tool
    @LLMDescription("Add a product to the shopping cart by product id")
    suspend fun addProductToCart(
        @LLMDescription("The id of the product to add to the shopping cart") productId: Int,
    ): String {
        val product = WebShopService.instance.getProductInCatalogue(productId)
            ?: return "Product with id $productId not found"

        WebShopService.instance.addProductToCart(product)
        return "Product ${product.name} with id ${product.id} and price ${product.price} successfully added to the shopping basket"
    }

    //endregion Task 2
}
