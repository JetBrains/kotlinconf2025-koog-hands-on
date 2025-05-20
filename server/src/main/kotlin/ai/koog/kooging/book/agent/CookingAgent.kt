package ai.koog.kooging.book.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.agent.entity.ToolSelectionStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTool
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.local.features.eventHandler.feature.handleEvents
import ai.koog.agents.local.features.tracing.feature.Tracing
import ai.koog.kooging.book.agent.tool.ShoppingTools
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.markdown.markdown
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class CookingAgent(private val llmModel: LLModel = OpenAIModels.Chat.GPT4o) {

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
                toolSelectionStrategy = ToolSelectionStrategy.NONE,
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

            val searchIngredients by subgraphWithTask(
                tools = listOf(
                    ShoppingTools::searchIngredient.asTool(),
                    ShoppingTools::putProductInShoppingBasket.asTool()
                ),
                shouldTLDRHistory = false,
            ) { ingredientList: String ->
                markdown {
                    h1("TASK")
                    bulleted {
                        +"Your task is to search for the ingredients in the food internet shop"
                        +"You can search for the ingredient by name using ${ShoppingTools::searchIngredient.name}, which returns the list of matches"
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
        }

    suspend fun execute(cookingRequest: String): String? {
        val executor = SingleLLMPromptExecutor(OpenAILLMClient(apiKey = token))

        val toolRegistry = ToolRegistry {
            tool(ShoppingTools::searchIngredient.asTool())
            tool(ShoppingTools::putProductInShoppingBasket.asTool())
        }

        val expectedResult = CompletableDeferred<String?>()

        val strategy = createCookingStrategy()

        val agentConfig = AIAgentConfig(
            prompt = prompt("system") {
                system(systemPrompt)
            },
            model = llmModel,
            maxAgentIterations = 100
        )

        AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
        ) {
            install(Tracing.Feature)

            handleEvents {
                onAgentFinished = { strategyName: String, result: String? ->
                    expectedResult.complete(result)
                }
            }
        }.run(cookingRequest)

        return expectedResult.await()
    }
}

fun main() = runBlocking {
    print("I would like to cook: ")
    val cookingRequest = readln()

    println("Starting agent with request: $cookingRequest")
    val agent = CookingAgent()
    val result = agent.execute(cookingRequest)

    println("Agent finished with result: $result")
}