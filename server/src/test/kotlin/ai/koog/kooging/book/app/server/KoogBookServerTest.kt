package ai.koog.kooging.book.app.server

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import java.net.ServerSocket
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class KoogBookServerTest {

    private val clientBaseUrl = "http://127.0.0.1"

    @Test
    fun testCookServerIsStarted() = runTest {

        val port = findAvailablePort()
        val serverConfig = KoogServerConfig(port = port)

        KoogBookServer(serverConfig).use { server ->
            server.startServer(wait = false)

            // Wait for the server to start
            delay(1000)

            HttpClient().use { client ->
                val response = client.get("$clientBaseUrl:$port/healthcheck")
                assertEquals(HttpStatusCode.Companion.OK, response.status)
            }
        }
    }

    @Test
    @Ignore("Test require OPEN_AI_TOKEN variable")
    fun testCookServerEndpoint() = runTest {
        val port = findAvailablePort()
        val serverConfig = KoogServerConfig(port = port)

        KoogBookServer(serverConfig).use { server ->
            server.startServer(wait = false)

            // Wait for the server to start
            delay(1000)

            HttpClient().use { client ->

                val response = client.post("$clientBaseUrl:$port/cook") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"input": "I would like to make a salad for dinner"}""")
                }

                assertEquals(HttpStatusCode.Companion.OK, response.status)

                // TODO: Check agent is started as well
            }
        }
    }

    @Test
    @Ignore("Test require OPEN_AI_TOKEN variable")
    fun testSSEEvents() = runTest {
        val port = findAvailablePort()
        val serverConfig = KoogServerConfig(port = port)

        KoogBookServer(serverConfig).use { server ->
            server.startServer(wait = false)

            // Wait for the server to start
            delay(1000)

            HttpClient().use { client ->
                // TODO: Fix test
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