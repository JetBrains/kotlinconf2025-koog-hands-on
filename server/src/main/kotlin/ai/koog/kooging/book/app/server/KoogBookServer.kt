package ai.koog.kooging.book.app.server

import ai.koog.kooging.book.app.model.Message
import ai.koog.kooging.book.app.model.Product
import ai.koog.kooging.book.app.service.AgentService
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
import org.slf4j.LoggerFactory
import java.lang.AutoCloseable
import java.util.*

class KoogBookServer(private val config: KoogServerConfig) : AutoCloseable {

    companion object {
        private val logger = LoggerFactory.getLogger(KoogBookServer::class.java)

        private val LastCookRequestKey = AttributeKey<String>("lastCookRequest")
    }

    private val server = embeddedServer(CIO, host = config.host, port = config.port) {
        configureServer()
    }

    private val agentService = AgentService()

    private val toSendMessages: Channel<Message> = Channel(Channel.UNLIMITED)

    //region Start / Stop

    fun startServer(wait: Boolean = true) {
        // Init Web Shop API before starting a server
        WebShopService.instance

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

            post("/cancel") {

            }

            // Cook SSE endpoint - starts agent and returns ingredients based on user input
            sse("/cook") {
                val userInput = call.request.queryParameters["input"] ?: ""
                logger.info("SSE cook request with input: $userInput")

                // Store the request in the application state (for demo purposes)
                application.attributes.put(LastCookRequestKey, userInput)

                // Start an agent
                agentService.startAgent(UUID.randomUUID().toString(), userInput) { message ->
                    sendAgentMessage(message)
                }

                send(
                    ServerSentEvent(
                        event = "finish",
                        data = ""
                    )
                )
            }

            get("/healthcheck") {
                call.respond(HttpStatusCode.OK, "Koog Book Server is running")
            }
        }

    private suspend fun ServerSSESession.sendAgentMessage(message: Message) {
        try {

            val serverEvent = ServerSentEvent(
                event = message.messageType.event,
                data = message.toServerEventData()
            )

            send(serverEvent)
        }
        catch (t: CancellationException) {
            logger.info("SSE stream cancelled")
            throw t
        }
        catch (t: Throwable) {
            logger.error("Error sending SSE event: ${t.message}", t)
        }
    }

    private fun Message.toServerEventData(): String {
        return defaultJson.encodeToString(this)
    }

    //endregion Private Methods
}
