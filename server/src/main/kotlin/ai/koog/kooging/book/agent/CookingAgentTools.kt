package ai.koog.kooging.book.agent

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.kooging.book.app.service.WebShopService
import kotlinx.serialization.json.Json

class CookingAgentTools {

    @Tool
    @LLMDescription("Search for an product in the food internet shop")
    fun searchProduct(
        @LLMDescription("Product name") query: String,
    ): String {
        val products = WebShopService.instance.searchProducts(query)
        return Json.Default.encodeToString(products)
    }

    @Tool
    @LLMDescription("Put a product in the shopping basket")
    suspend fun putProductInShoppingBasket(
        @LLMDescription("An id of the product to put in the shopping basket") id: Int,
    ): String {
        WebShopService.instance.let { service ->
            val product = service.findProduct(id) ?: return@let
            service.putToBasket(id = product.id)
            return "Product ${product.name} with id ${product.id} and price ${product.price} successfully added to the shopping basket"
        }

        return "Product with id $id not found"
    }
}