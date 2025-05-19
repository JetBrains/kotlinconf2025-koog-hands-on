package ai.koog.kooging.book

import ai.koog.kooging.book.app.model.Ingredient
import ai.koog.kooging.book.app.server.KoogBookServer
import ai.koog.kooging.book.app.server.KoogServerConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KoogBookServerTest {

    private val clientBaseUrl = "http://127.0.0.1"

    @Test
    fun testCookEndpoint() = runTest {

        val port = findAvailablePort()
        val serverConfig = KoogServerConfig(port = port)

        KoogBookServer(serverConfig).use { server ->
            server.startServer(wait = false)

            // Wait for the server to start
            delay(1000)

            HttpClient().use { client ->

                // Test the POST endpoint
                val response = client.post("$clientBaseUrl:$port/cook") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"input": "I would like to make a salad for dinner"}""")
                }

                // Verify the response
                assertEquals(HttpStatusCode.OK, response.status)

                // Test the GET endpoint for SSE
                val sseResponse = client.get("$clientBaseUrl:$port/cook")

                // Verify the response content type
                assertTrue(
                    sseResponse.contentType().toString().contains(ContentType.Text.EventStream.toString()),
                    "Content type should be event-stream"
                )

                // Verify the response body contains the expected SSE format
                val responseText = sseResponse.bodyAsText()
                assertTrue(responseText.contains("event: ingredients"))
                assertTrue(responseText.contains("data: ["))

                // Parse the JSON data from the SSE event
                val dataLine = responseText.lines().find { it.startsWith("data: ") }
                assertNotNull(dataLine, "SSE data line not found")

                val jsonData = dataLine.substring("data: ".length)
                val ingredients = Json.decodeFromString<List<Ingredient>>(jsonData)

                // Verify we have 5 ingredients as expected
                assertEquals(5, ingredients.size, "Should have 5 ingredients")

                // Verify each ingredient has the required fields
                ingredients.forEach { ingredient ->
                    assertNotNull(ingredient.id, "Ingredient ID should not be null")
                    assertNotNull(ingredient.name, "Ingredient name should not be null")
                    assertTrue(ingredient.price > 0, "Ingredient price should be positive")
                }
            }
        }
    }

    private fun findAvailablePort(): Int {
        val port = ServerSocket(0).use { socket ->
            socket.localPort
        }

        return port
    }
}
