package ai.koog.kooging.book.agent

import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.reflect.asTool
import ai.koog.agents.ext.agent.subgraphWithTask

fun exampleStrategy(): AIAgentStrategy =
    strategy("example") {
        // Start node of the strategy is String -> String node.
        // It accepts and returns agent input
        println(nodeStart.name)

        // Finish node of the strategy is also String -> String node.
        // It accepts the incoming input and returns it as an agent result
        println(nodeFinish.name)

        // You can call LLM node with/without tools.
        val nodeRequestLLM by nodeLLMRequest(name = "Request LLM", allowToolCalls = false)
        println(nodeRequestLLM.name)

        // To connect nodes use edge(fromNode forwardTo toNode)
        edge(nodeStart forwardTo nodeFinish transformed { it + "Visited edge from start to finish" })
        // You can also use onCondition to define conditions for the edge
        edge(nodeStart forwardTo nodeFinish onCondition { it.contains("Hello") })
        // You can use onToolCall predefined condition which will check if the llm output is a tool call
        edge(nodeRequestLLM forwardTo nodeFinish onToolCall { true } transformed { "Tool message: ${it.tool}" })
        // You can use onAssistantMessage predefined condition which will check if the llm output is an assistant message
        edge(nodeRequestLLM forwardTo nodeFinish onAssistantMessage { true } transformed { "Assistant message: $it" })

    }

fun planCookingStrategy(): AIAgentStrategy =
    strategy("simple-cooking-agent") {

        // TODO: [Step 1.1] Define node to call LLM without any tools (see nodeLLMRequest)
        val nodePlanIngredients by nodeLLMRequest(allowToolCalls = false)

        // TODO: [Step 1.2] Add edge from start to call llm node
        edge(nodeStart forwardTo nodePlanIngredients)

        // TODO: [Step 1.3] Add edge from llm node to finish node
        //  The condition of the step should be onAssistantMessage
        edge(nodePlanIngredients forwardTo nodeFinish onAssistantMessage { true })
    }


fun planAndOrderCookingStrategy(tools: CookingAgentTools): AIAgentStrategy =
    strategy("advanced-cooking-agent") {
        val nodePlanIngredients by nodeLLMRequest(allowToolCalls = false)

        // TODO: [Step 2.1] Add subgraph with task to call llm node with tools.
        val subgraphOrderIngredients by subgraphWithTask<Unit>(
            tools = listOf(
                tools::searchProduct.asTool(),
                tools::putProductInShoppingBasket.asTool()
            ),
            shouldTLDRHistory = false,
        ) { _ -> "Start order ingredients" }

        // TODO: [Step 2.2] Add edge from start to call llm node
        edge(nodeStart forwardTo nodePlanIngredients)

        // TODO: [Step 2.3] Add edge from plan ingredients llm node to subgraph node
        edge(nodePlanIngredients forwardTo subgraphOrderIngredients onAssistantMessage { true } transformed { })

        // TODO: [Step 2.4] Add edge from subgraph node to finish node
        edge(subgraphOrderIngredients forwardTo nodeFinish transformed { it.result })
    }
