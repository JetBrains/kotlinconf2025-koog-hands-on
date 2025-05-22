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

        // TODO: Define an implementation for the tool
        //  that search for the products in a shop by an input query (ingredient name) and
        //  return the list of found products.
        //  Tips:
        //      1. Use the [WebShopService.instance] singleton to work with a shop API;
        //      2. Use the method searchProductsInCatalogue() to search for a products by an input query;

        return emptyList(
            // TODO: Return a list of products
        )
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

        // TODO: Define an implementation for the tool
        //  that adds a product object into a shopping cart by a product Id.
        //  Tips:
        //      1. Use the [WebShopService.instance] singleton to work with shopping cart API;
        //      2. Use the getProductInCatalogue() method to search for a product by id;
        //      3. Use the addProductToCart() method to add a product to the shopping cart;
        //      4. Check conditions and ensure that you return a clear [String] output for each case (the LLM will use this output further).

        return "TODO"
    }

    //endregion Task 2
}
