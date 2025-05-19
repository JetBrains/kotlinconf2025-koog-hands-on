package ai.koog.kooging.book.agent.tool

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

object ShoppingTools {

    // TODO: Update 'searchIngredient' tool
    @Tool
    @LLMDescription("Search for an ingredient in the food internet shop")
    fun searchIngredient(
        @LLMDescription("Ingredient name")
        ingredient: String,
    ): String {
        return Json.encodeToString(
            value = buildJsonObject {
                put("id", JsonPrimitive((0..1000).random()))
                put("name", JsonPrimitive(ingredient))
            }
        )
    }

    // TODO: Update 'putProductInShoppingBasket' tool
    @Tool
    @LLMDescription("Put a product in the shopping basket")
    fun putProductInShoppingBasket(
        @LLMDescription("An id of the product to put in the shopping basket")
        id: Int,
    ): String {
        return "Success"
    }
}
