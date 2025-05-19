package ai.koog.kooging.book.app.server

import ai.koog.kooging.book.app.model.CookRequest
import ai.koog.kooging.book.app.model.Ingredient
import ai.koog.kooging.book.model.Product
import ai.koog.kooging.book.model.WebShop
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.lang.AutoCloseable

class KoogBookServer(private val config: KoogServerConfig): AutoCloseable {

    companion object {
        private val logger = LoggerFactory.getLogger("ai.koog.kooging.book.app.server.KoogBookServer")

        private val LastCookRequestKey = AttributeKey<String>("lastCookRequest")
    }

    private val server = embeddedServer(CIO, port = config.port) {
        configureServer()
    }

    fun startServer(wait: Boolean = true) {
        server.start(wait = wait)
        logger.info("Server started on port ${config.port}")
    }

    override fun close() {
        server.stop(1000, 1000)
        logger.info("Server stopped")
    }

    //region Private Methods

    private fun Application.configureServer() {

        val webShop = WebShop()

        // Configure plugins
        install(DefaultHeaders)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        // Configure routing
        routing {
            staticResources("/", "static") {
                default("index.html")
            }

            // Cook POST endpoint - receives the user prompt
            post("/cook") {
                val request = call.receive<CookRequest>()
                val userInput = request.input
                println("Received cook request with input: $userInput")


                // Store the request in the application state (for demo purposes)
                application.attributes.put(LastCookRequestKey, userInput)

                // Respond with success
                call.respond(HttpStatusCode.OK)
            }

            // Cook GET endpoint - provides SSE stream with ingredients
            get("/cook") {
                // Set up SSE response
                call.response.cacheControl(CacheControl.NoCache(null))
                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    // Generate 5 random ingredients
                    val allProducts = webShop.getAllProducts()
                    val randomIngredients = generateRandomIngredients(allProducts, 5)

                    // Send ingredients as SSE event
                    val ingredientsJson = Json.encodeToString(
                        ListSerializer(Ingredient.serializer()),
                        randomIngredients
                    )

                    // Add a small delay to simulate processing
                    delay(1000)

                    // Write SSE event
                    write("event: ingredients\n")
                    write("data: $ingredientsJson\n\n")
                    flush()
                }
            }
        }
    }

    // TODO: Delete
    private fun generateRandomIngredients(products: List<Product>, count: Int): List<Ingredient> {
        return products
            .shuffled()
            .take(count)
            .map { Ingredient(it.name, it.id.toString(), it.price) }
    }

    //endregion Private Methods
}
