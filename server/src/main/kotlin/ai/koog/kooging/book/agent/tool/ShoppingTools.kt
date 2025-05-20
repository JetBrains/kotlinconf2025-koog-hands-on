package ai.koog.kooging.book.agent.tool

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.kooging.book.app.service.WebShopService
import kotlinx.serialization.json.Json

object ShoppingTools : ToolSet {

class ShoppingTools(private val webShop: WebShopService) {

    @Tool
    @LLMDescription("Search for an product in the food internet shop")
    fun searchProduct(
        @LLMDescription("Product name") query: String,
    ): String {
        val products = webShop.searchProducts(query)
        return Json.encodeToString(products)
    }

    @Tool
    @LLMDescription("Put a product in the shopping basket")
    fun putProductInShoppingBasket(
        @LLMDescription("An id of the product to put in the shopping basket") id: Int,
    ): String {
        webShop.findProduct(id)?.let {
            webShop.putToBasket(it.id)
            return "Product ${it.name} with id ${it.id} and price ${it.price} successfully added to the shopping basket"
        }

        return "Product with id $id not found"
    }
}
