package ai.koog.kooging.book.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.AIAgent.FeatureContext
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTool
import ai.koog.agents.ext.agent.simpleSingleRunAgent
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.local.features.eventHandler.feature.EventHandler
import ai.koog.agents.local.features.tracing.feature.Tracing
import ai.koog.kooging.book.agent.tool.ShoppingTools
import ai.koog.kooging.book.app.model.LLMErrorMessage
import ai.koog.kooging.book.app.model.LLMMessage
import ai.koog.kooging.book.app.model.LLMMessageType
import ai.koog.kooging.book.app.model.Message
import ai.koog.kooging.book.app.service.WebShopService
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.markdown.markdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class CookingAgent(
    private val llmModel: LLModel = OpenAIModels.Chat.GPT4o,
    private val webShop: WebShopService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(CookingAgent::class.java)

        fun CoroutineScope.startCookAgent(
            cookingRequest: String,
            webShop: WebShopService,
            onAgentEvent: suspend (Message) -> Unit
        ) {
            val agent = CookingAgent(webShop = webShop)
            launch(Dispatchers.IO) {
                agent.executeSimple(cookingRequest, onAgentEvent)
            }
        }
    }

    private val token: String =
        System.getenv("OPEN_AI_TOKEN") ?: error("OPEN_AI_TOKEN environment variable is not set")

    private val systemPrompt = markdown {
        h1("GENERAL INSTRUCTIONS")
        bulleted {
            +"You are an agent named Koog that helps with cooking"
            +"You must be helpful and respectful"
        }
        newline()
        h1("TASK")
        bulleted {
            +"You accept the user request, who wants to cook some dish"
            +"Then you need to make a list of ingredients required to cook it"
            +"Those are food ingredients: assume that the person already has all the cooking utensils"
            +"After that, you need to search those ingredients in the food internet shop"
            +"If the ingredient is found, it will be automatically added to the shopping basket"
            +"In the end, you need to show the list of all ingredients to the user"
        }
    }

    private fun createCookingStrategy(): AIAgentStrategy =
        strategy("cooking-agent") {
            val splitDishIntoIngredients by subgraphWithTask(
                tools = emptyList(),
                shouldTLDRHistory = false,
            ) { dish: String ->
                markdown {
                    h1("TASK")
                    bulleted {
                        +"Your task is to split the dish into its ingredients"
                        +"Output it as a list of bullet-pointed strings in Markdown format"
                        +"Ingredient names must be short and concise"
                    }

                    h1("OUTPUT EXAMPLE")
                    bulleted {
                        +"Potato"
                        +"Beet"
                        +"Carrot"
                        +"Pickle"
                        +"Onion"
                        +"Peas"
                        +"Oil"
                        +"Salt"
                    }

                    h1("DISH DESCRIPTION")
                    +dish
                }
            }

            val tools = ShoppingTools(webShop)
            val searchIngredients by subgraphWithTask(
                tools = listOf(
                    tools::searchProduct.asTool(),
                    tools::putProductInShoppingBasket.asTool()
                ),
                shouldTLDRHistory = false,
            ) { ingredientList: String ->
                markdown {
                    h1("TASK")
                    bulleted {
                        +"Your task is to search for the ingredients in the food internet shop"
                        +"You can search for the ingredient by name using ${ShoppingTools::searchProduct.name}, which returns the list of matches"
                        +"Then you can put one of the matching ingredient in the shopping basket using ${ShoppingTools::putProductInShoppingBasket.name}"
                        +"In case there are several matches, choose only 1 which fits the best"
                    }

                    h1("LIST OF INGREDIENTS")
                    +ingredientList
                }
            }

            edge(nodeStart forwardTo splitDishIntoIngredients)
            edge(splitDishIntoIngredients forwardTo searchIngredients transformed { it.result })
            edge(searchIngredients forwardTo nodeFinish transformed { it.result })

            // TODO: Delete - Simple test
//            val askNode by nodeLLMRequest("Ask LLM")
//            val nodeExecuteTool by nodeExecuteTool("nodeExecuteTool")
//            val nodeSendToolResult by nodeLLMSendToolResult("nodeSendToolResult")
//            edge(nodeStart forwardTo askNode)
//            edge(askNode forwardTo nodeExecuteTool onToolCall { true })
//            edge(nodeExecuteTool forwardTo nodeSendToolResult)
//            edge(nodeSendToolResult forwardTo nodeFinish onAssistantMessage { true })
//            edge(askNode forwardTo nodeFinish onAssistantMessage { true })
        }

    suspend fun execute(cookingRequest: String, onAgentEvent: suspend (Message) -> Unit): String? {
        val executor = SingleLLMPromptExecutor(OpenAILLMClient(apiKey = token))

        val shoppingTools = ShoppingTools(webShop)
        val toolRegistry = ToolRegistry {
            tool(shoppingTools::searchProduct.asTool())
            tool(shoppingTools::putProductInShoppingBasket.asTool())
        }

        val strategy = createCookingStrategy()

        val agentConfig = AIAgentConfig(
            prompt = prompt("system") {
                system(systemPrompt)
            },
            model = llmModel,
            maxAgentIterations = 100
        )

        val agent = AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
            installFeatures = { configureFeatures(onAgentEvent) }
        )

        val agentResult = agent.runAndGetResult(cookingRequest)
        logger.info("Agent finished with result: $agentResult")

        return agentResult
    }

    suspend fun executeSimple(cookingRequest: String, onAgentEvent: suspend (Message) -> Unit): String? {
        val executor = SingleLLMPromptExecutor(OpenAILLMClient(apiKey = token))

        val shoppingTools = ShoppingTools(webShop)
        val toolRegistry = ToolRegistry {
            tool(shoppingTools::searchProduct.asTool())
            tool(shoppingTools::putProductInShoppingBasket.asTool())
        }

        val agent = simpleSingleRunAgent(
            executor = executor,
            systemPrompt = systemPrompt,
            llmModel = llmModel,
            toolRegistry = toolRegistry,
            maxIterations = 100,
            installFeatures = { configureFeatures(onAgentEvent) }
        )

        val agentResult = agent.runAndGetResult(cookingRequest)
        logger.info("Agent finished with result: $agentResult")

        return agentResult
    }

    private fun FeatureContext.configureFeatures(onAgentEvent: suspend (Message) -> Unit) {
        install(Tracing)

        install(EventHandler) {
            onToolCallResult = { tool, toolArgs, result ->
                val message = LLMMessage(
                    messageType = LLMMessageType.TOOL_CALL,
                    content = "Call tool: '${tool.name}', args: '$toolArgs', result: '$result'"
                )
                onAgentEvent(message)
            }

            onAgentFinished = { strategyName: String, result: String? ->
                val message = LLMMessage(
                    messageType = LLMMessageType.ASSISTANT,
                    content = result ?: "UNKNOWN RESULT"
                )
                onAgentEvent(message)
            }

            onAgentRunError = { strategyName, throwable ->
                val message = LLMErrorMessage(message = throwable.message ?: "UNKNOWN ERROR")
                onAgentEvent(message)
            }
        }
    }
}

fun main() = runBlocking {
    print("I would like to cook: ")
    val cookingRequest = readln()

    println("Starting agent with request: $cookingRequest")
    val webShopService = WebShopService()
    val agent = CookingAgent(webShop = webShopService)
    val result = agent.executeSimple(cookingRequest) {}

    println("Agent finished with result: $result")
}