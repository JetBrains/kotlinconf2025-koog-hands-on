package ai.koog.kooging.book.app.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface Message {
    val messageType: LLMMessageType
    val message: String
}

enum class LLMMessageType(val event: String) {
    ASSISTANT("assistant"),
    TOOL_CALL("tool_call"),
    INGREDIENTS("ingredients"),
    ERROR("error"),
}

@Serializable
data class LLMMessage(
    override val message: String
) : Message {
    override val messageType: LLMMessageType = LLMMessageType.ASSISTANT
}

@Serializable
@ConsistentCopyVisibility
data class LLMToolCallMessage private constructor(
    override val message: String
) : Message {
    constructor(toolName: String, toolArgs: String, result: String? = null) : this(
        "Tool Call. Tool:  $toolName, args: $toolArgs, result: $result"
    )

    override val messageType: LLMMessageType = LLMMessageType.TOOL_CALL
}

@Serializable
data class LLMErrorMessage(
    override val message: String
) : Message {
    override val messageType: LLMMessageType = LLMMessageType.ERROR
}

@Serializable
data class IngredientsMessage(
    val ingredients: List<String>
) : Message {
    override val messageType = LLMMessageType.INGREDIENTS
    override val message: String = "Received Ingredients list from LLM"
}
