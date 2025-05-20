package ai.koog.kooging.book.app.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Message

enum class LLMMessageType {
    ASSISTANT,
    TOOL_CALL,
    ERROR,
}

@Serializable
data class LLMMessage(
    val messageType: LLMMessageType,
    val content: String
) : Message()

@Serializable
data class LLMErrorMessage(
    val messageType: LLMMessageType,
    val message: String
) : Message()

@Serializable
data class IngredientsMessage(
    val messageType: LLMMessageType,
    val ingredients: List<String>
) : Message()