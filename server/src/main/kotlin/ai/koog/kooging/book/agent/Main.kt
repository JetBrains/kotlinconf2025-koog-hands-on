package ai.koog.kooging.book.agent

import ai.koog.kooging.book.app.service.WebShopService
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    print("I would like to cook: ")
    val cookingRequest = readln()

    println("Starting agent with request: $cookingRequest")
    val webShopService = WebShopService()
    val agent = CookingAgent(webShop = webShopService)
    val result = agent.execute(cookingRequest) {}

    println("Agent finished with result: $result")
}
