package ai.koog.book.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.book.app.model.Message
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import org.slf4j.LoggerFactory

/**
 * A class that manages a cooking agent.
 */
class CookingAgent() {

    companion object {
        private val logger = LoggerFactory.getLogger(CookingAgent::class.java)
    }

    private val token: String =
        System.getenv("OPEN_AI_TOKEN") ?: error("OPEN_AI_TOKEN environment variable is not set")

    /**
     * Executes the cooking request using an AI agent with specific strategies and tools, and processes
     * events emitted during the execution.
     *
     * @param cookingRequest The description of the cooking task or recipe provided by the user.
     * @param onAgentEvent A callback function triggered for various agent events, providing messages
     * related to tool usage, LLM calls, or final results during execution.
     * @return The result of the AI agent's execution as a string if successful, or `null` if an error occurs.
     */
    suspend fun execute(cookingRequest: String, onAgentEvent: suspend (Message) -> Unit): String? {

        val agentConfig = AIAgentConfig(
            prompt = prompt("cook_agent_system_prompt") {
                system(CookingAgentPrompts.planCookingSystemPrompt)
            },
            model = OpenAIModels.Chat.GPT4o,
            maxAgentIterations = 100
        )

        val executor = SingleLLMPromptExecutor(OpenAILLMClient(apiKey = token))

        val strategy = CookingAgentStrategies.planCookingStrategy()

        val agent = AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = ToolRegistry { },
            installFeatures = { configureFeatures(onAgentEvent) }
        )

        val agentResult = agent.runAndGetResult(cookingRequest)

        logger.info("Agent finished with result: $agentResult")

        return agentResult
    }
}
