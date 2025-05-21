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
                val ingredients = response.firstOrNull()?.getIngredients() ?: emptyList()
                val message = IngredientsMessage(
                    ingredients = ingredients
                )
                onAgentEvent(message)
            }

            val messageBuilder = StringBuilder()
            messageBuilder.appendLine("LLM Responses:")
            response.forEach { responseMessage -> messageBuilder.appendLine("  - ${responseMessage.content}") }

            if (tools.isNotEmpty()) {
                messageBuilder
                    .append("Tools: ")
                    .append("[")
                    .append(tools.joinToString { it.name })
                    .append("]")
                    .appendLine()
            }

            val message = LLMMessage(message = messageBuilder.toString())
            onAgentEvent(message)
        }

        onAgentFinished = { strategyName: String, result: String? ->
            val message = LLMMessage(
                message = "Agent finished with result: $result"
            )
            onAgentEvent(message)
        }

        onAgentRunError = { strategyName, throwable ->
            val message = LLMErrorMessage(
                message = "Agent execution error: ${throwable.message}"
            )
            onAgentEvent(message)
        }
    }
}

private val ingredientItemTrimRegex = "^[\\d\\s\\-.,;: ]*".toRegex()
private val ingredientItemRegex = "^[\\d-+•*]".toRegex()

private fun ai.koog.prompt.message.Message.Response.getIngredients(): List<String> {
    // Select only the list of ingredients
    val trimmedContent = content.lines().filter { line ->
        ingredientItemRegex.containsMatchIn(line.trim())
    }

    return trimmedContent.map { ingredient ->
        ingredientItemTrimRegex.replaceFirst(ingredient.trim(), "")
    }
}