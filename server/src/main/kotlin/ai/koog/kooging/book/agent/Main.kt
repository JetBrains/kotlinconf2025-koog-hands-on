package ai.koog.kooging.book.agent

import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    print("I would like to cook: ")
    val cookingRequest = readln()

    println("Starting agent with request: $cookingRequest")
    val agent = CookingAgent("")
    val result = agent.execute(cookingRequest) {}

    println("Agent finished with result: $result")
}
