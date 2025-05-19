package ai.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.ToolSelectionStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.asTool
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.local.features.eventHandler.feature.handleEvents
import ai.koog.agents.local.features.tracing.feature.Tracing
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.markdown.markdown
import io.klogging.config.ANSI_CONSOLE
import io.klogging.config.loggingConfiguration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Tool
@LLMDescription("")
fun searchItem(
    @LLMDescription("")
    ingredient: String,
): String {
    return Json.encodeToString(
        value = buildJsonObject {
            put("id", JsonPrimitive((0..1000).random()))
            put("name", JsonPrimitive(ingredient))
        }
    )
}

val searchItemTool = ::searchItem.asTool()


@Tool
@LLMDescription("")
fun putInShoppingBasket(
    @LLMDescription("")
    id: Int,
): String {
    return "Success"
}

val putInShoppingBasketTool = ::putInShoppingBasket.asTool()


object CookingAgent {
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

    private val strategy = strategy("cooking-agent") {
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
                searchItemTool,
                putInShoppingBasketTool
            ),
            shouldTLDRHistory = false,
        ) { ingredientList: String ->
            markdown {
                h1("TASK")
                bulleted {
                    +"Your task is to search for the ingredients in the food internet shop"
                    +"You can search for the ingredient by name using ${searchItemTool.name}, which returns the list of matches"
                    +"Then you can put one of the matching ingredient in the shopping basket using ${putInShoppingBasketTool.name}"
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
        val executor = SingleLLMPromptExecutor(OpenAILLMClient(apiKey = System.getenv("OPENAI_TOKEN")))

        val toolRegistry = ToolRegistry {
            tool(searchItemTool)
            tool(putInShoppingBasketTool)
        }

        val expectedResult = CompletableDeferred<String?>()

        AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = AIAgentConfig(
                prompt = prompt("system") { system(systemPrompt) },
                model = OpenAIModels.Chat.GPT4_1,
                maxAgentIterations = 100
            ),
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

suspend fun main() {
    loggingConfiguration {
        ANSI_CONSOLE()
    }

    val cookingRequest = readln()
    val result = CookingAgent.execute(cookingRequest)
    println(result)
}