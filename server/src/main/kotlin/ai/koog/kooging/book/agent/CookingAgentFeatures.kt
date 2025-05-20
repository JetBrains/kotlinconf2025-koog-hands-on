package ai.koog.kooging.book.agent

import ai.koog.agents.core.agent.AIAgent.FeatureContext
import ai.koog.agents.local.features.eventHandler.feature.EventHandler
import ai.koog.agents.local.features.tracing.feature.Tracing
import ai.koog.kooging.book.app.model.LLMErrorMessage
import ai.koog.kooging.book.app.model.LLMMessage
import ai.koog.kooging.book.app.model.LLMMessageType
import ai.koog.kooging.book.app.model.Message

fun FeatureContext.configureFeatures(onAgentEvent: suspend (Message) -> Unit) {
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