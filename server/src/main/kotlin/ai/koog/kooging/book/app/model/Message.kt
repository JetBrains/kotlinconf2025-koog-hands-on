package ai.koog.kooging.book.app.model

import kotlinx.serialization.Serializable

interface Message

enum class LLMMessageType {
    ASSISTANT,
    TOOL_CALL,
}

@Serializable
data class LLMMessage(
    val type: LLMMessageType,
    val content: String
) : Message

@Serializable
data class LLMErrorMessage(
    val message: String
) : Message
