package ai.koog.kooging.book.agent

import ai.koog.agents.core.agent.AIAgent.FeatureContext
import ai.koog.agents.local.features.eventHandler.feature.EventHandler
import ai.koog.agents.local.features.tracing.feature.Tracing
import ai.koog.kooging.book.app.model.*

fun FeatureContext.configureFeatures(onAgentEvent: suspend (Message) -> Unit) {
    install(Tracing)

    install(EventHandler) {
        onToolCallResult = { tool, toolArgs, result ->
            val message = LLMToolCallMessage(
                toolName  = tool.name,
                toolArgs = toolArgs.toString(),
                result = result?.toStringDefault() ?: "UNKNOWN TOOL CALL RESULT"
            )
            onAgentEvent(message)
        }

        var isFirstLLMCall = true

        onAfterLLMWithToolsCall = { response, tools ->
            if (isFirstLLMCall) {
                isFirstLLMCall = false
                val ingredients = response.firstOrNull()?.let { response ->
                    response.content.split("\n")
                        .map { ingredient ->
                            "^[\\d\\s\\-.,;: ]*".toRegex().replaceFirst(ingredient.trim(), "")
                        }
                } ?: emptyList()
                val message = IngredientsMessage(
                    ingredients = ingredients
                )
                onAgentEvent(message)
            }
        }

        onAgentFinished = { strategyName: String, result: String? ->
            val message = LLMMessage(
                content = result ?: "UNKNOWN RESULT"
            )
            onAgentEvent(message)
        }

        onAgentRunError = { strategyName, throwable ->
            val message = LLMErrorMessage(
                message = throwable.message ?: "UNKNOWN ERROR"
            )
            onAgentEvent(message)
        }
    }
}