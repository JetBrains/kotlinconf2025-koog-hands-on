package ai.koog.kooging.book.app.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface Message {
    val messageType: LLMMessageType
}

enum class LLMMessageType(val event: String) {
    ASSISTANT("assistant"),
    TOOL_CALL("tool_call"),
    INGREDIENTS("ingredients"),
    ERROR("error"),
}

@Serializable
data class LLMMessage(
    val content: String
) : Message {
    override val messageType: LLMMessageType = LLMMessageType.ASSISTANT
}

@Serializable
data class LLMToolCallMessage(
    val toolName: String,
    val toolArgs: String,
    val result: String? = null
) : Message {
    override val messageType: LLMMessageType = LLMMessageType.TOOL_CALL
}

@Serializable
data class LLMErrorMessage(
    val message: String
) : Message {
    override val messageType: LLMMessageType = LLMMessageType.ERROR
}

@Serializable
data class IngredientsMessage(
    val ingredients: List<String>
) : Message {
    override val messageType = LLMMessageType.INGREDIENTS
}
