package ai.koog.kooging.book.app.server

import ai.koog.kooging.book.agent.CookingAgent.Companion.startCookAgent
import ai.koog.kooging.book.app.model.Message
import ai.koog.kooging.book.app.model.Product
import ai.koog.kooging.book.app.service.WebShopService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import io.ktor.util.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import org.slf4j.LoggerFactory
import java.lang.AutoCloseable

class KoogBookServer(private val config: KoogServerConfig) : AutoCloseable {

    companion object {
        private val logger = LoggerFactory.getLogger(KoogBookServer::class.java)

        private val LastCookRequestKey = AttributeKey<String>("lastCookRequest")
    }

    private val server = embeddedServer(CIO, host = config.host, port = config.port) {
        configureServer()
    }

    private val toSendMessages: Channel<Message> = Channel(Channel.UNLIMITED)

    private val webShop = WebShopService()

    //region Start / Stop

    fun startServer(wait: Boolean = true) {
        server.start(wait = wait)
        logger.info("Server started on port ${config.port}")
    }

    override fun close() {
        server.stop(1000, 1000)
        logger.info("Server stopped")
    }

    //endregion Start / Stop

    //region Messages

    suspend fun sendMessage(message: Message) {
        toSendMessages.send(message)
    }

    //endregion Messages

    //region Private Methods

    private fun Application.configureServer() {

        install(DefaultHeaders)
        install(ContentNegotiation) {
            json(defaultJson)
        }
        install(SSE)

        configureRouting()
    }

    private fun Application.configureRouting(): Routing =
        routing {
            staticResources("/", "static") { default("index.html") }
            staticResources("/static/image", "static/image")

            // Cook SSE endpoint - starts agent and returns ingredients based on user input
            sse("/cook") {
                val userInput = call.request.queryParameters["input"] ?: ""
                logger.info("SSE cook request with input: $userInput")

                // Store the request in the application state (for demo purposes)
                application.attributes.put(LastCookRequestKey, userInput)

                // Start an agent
                startCookAgent(userInput, webShop = webShop) { message ->
                    sendMessage(message = message)
                }

                try {
                    val products = webShop.getAllProducts()
                    generateRandomIngredients(products, 5).map {
                        send(
                            ServerSentEvent(
                                event = "ingredients",
                                data = defaultJson.encodeToString(listOf(it))
                            ))
                        delay(500)
                    }

                    // finish message
                    send(
                        ServerSentEvent(
                            event = "done",
                            data = ""
                        )
                    )
                } catch (t: Throwable) {
                    logger.error("Error sending ingredients SSE event: ${t.message}", t)
                }
            }

            sse("/sse") {
                toSendMessages.consumeAsFlow().collect { message ->
                    try {
                        val serverEvent = ServerSentEvent(
                            event = "message",
                            data = message.toServerEventData()
                        )

                        send(serverEvent)
                    } catch (t: CancellationException) {
                        logger.info("SSE stream cancelled")
                        throw t
                    } catch (t: Throwable) {
                        logger.error("Error sending SSE event: ${t.message}", t)
                    }
                }
            }

            get("/healthcheck") {
                call.respond(HttpStatusCode.OK, "Koog Book Server is running")
            }
        }

    // TODO: Delete
    private fun generateRandomIngredients(products: List<Product>, count: Int): List<Product> {
        return products
            .shuffled()
            .take(count)
            .map { product -> Product(id = product.id, name = product.name, price = product.price) }
    }

    private fun Message.toServerEventData(): String {
        return defaultJson.encodeToString(this)
    }

    //endregion Private Methods
}
