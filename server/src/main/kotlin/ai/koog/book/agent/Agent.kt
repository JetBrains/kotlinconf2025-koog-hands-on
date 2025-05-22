package ai.koog.book.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
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
                system(CookingAgentPrompts.planAndOrderCookingSystemPrompt)
            },
            model = OpenAIModels.Chat.GPT4o,
            maxAgentIterations = 100
        )

        val executor = SingleLLMPromptExecutor(OpenAILLMClient(apiKey = token))

        val agentToolSet = CookingAgentTools()

        val toolRegistry = ToolRegistry {
            // TODO: Define the ToolRegistry with tools from LLM to process the initial request
            //  Tips:
            //      1. To add a single tool use tool() method. To add a set of tools, use tools() method
            //      2. You can use .asTools() extension for [ToolSet] interface
            //         or add tools one by one with .asTool() extension for KFunction.

        }

        val strategy = CookingAgentStrategies.planAndOrderCookingStrategy(
            tools = agentToolSet
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
}
