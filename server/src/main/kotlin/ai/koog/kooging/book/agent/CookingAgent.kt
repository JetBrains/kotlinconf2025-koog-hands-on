package ai.koog.kooging.book.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTool
import ai.koog.kooging.book.app.model.Message
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import org.slf4j.LoggerFactory

class CookingAgent(private val name: String) {

    private val token: String =
        System.getenv("OPEN_AI_TOKEN") ?: error("OPEN_AI_TOKEN environment variable is not set")

    suspend fun execute(cookingRequest: String, onAgentEvent: suspend (Message) -> Unit): String? {
        val cookingAgentTools = CookingAgentTools()
        val toolRegistry = ToolRegistry {
            tool(cookingAgentTools::searchProduct.asTool())
            tool(cookingAgentTools::putProductInShoppingBasket.asTool())
        }

        // 1.
        // planCookingStrategy()
        val strategy = planCookingStrategy()

        // 2.
//        val strategy = planAndOrderCookingStrategy(
//            tools = cookingAgentTools
//        )

        val agentConfig = AIAgentConfig(
            prompt = prompt("system") {
                // 1.
                 system(CookingAgentPrompts.planCookingSystemPrompt)
                // 2.
                // system(CookingAgentPrompts.planAndOrderCookingSystemPrompt)
            },
            model = OpenAIModels.Chat.GPT4o,
            maxAgentIterations = 100
        )

        val executor = SingleLLMPromptExecutor(OpenAILLMClient(apiKey = token))

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

    companion object {
        private val logger = LoggerFactory.getLogger(CookingAgent::class.java)
    }
}
