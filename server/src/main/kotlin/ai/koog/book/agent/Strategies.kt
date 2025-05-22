package ai.koog.book.agent

import ai.koog.agents.core.agent.entity.AIAgentStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.agent.subgraphWithTask

/**
 * The [CookingAgentStrategies] object contains various strategies
 * for creating and executing cooking-related workflows for an AI agent.
 */
object CookingAgentStrategies {

    /**
     * This strategy for the example purposes only
     */
    fun exampleStrategy(): AIAgentStrategy = strategy("example") {
        // The start node of the strategy is String -> String node.
        // It accepts and returns agent input
        println(nodeStart.name)

        // The finish node of the strategy is also String -> String node.
        // It accepts the incoming input and returns it as an agent result
        println(nodeFinish.name)

        // You can call an LLM node with/without tools.
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

    //region Task 1

    /**
     * This strategy for planning the ingredient requirements for the dish using a single LLM call without any tools.
     *
     * The strategy consists of a simple flow:
     * 1. Get dish description from the user (from strategy's nodeStart)
     * 2. Calls the language model to deduce the list of ingredients required to prepare a given dish
     * 3. Transitions the llm response content to the agent's output (to strategy's nodeFinish)
     *
     * @return AIAgentStrategy object that defines the cooking planning behavior of the AI agent.
     */
    fun planCookingStrategy(): AIAgentStrategy = strategy("simple-cooking-agent") {
        val nodePlanIngredients by nodeLLMRequest(allowToolCalls = false)
        edge(nodeStart forwardTo nodePlanIngredients)
        edge(nodePlanIngredients forwardTo nodeFinish onAssistantMessage { true })
    }

    //endregion Task 1

    //region Task 2

    /**
     * This strategy for planning the ingredient requirements for the dish using a single LLM call with tools.
     * The strategy consists of a simple flow:
     * 1. Get dish description from the user (from strategy's nodeStart)
     * 2. Calls the language model to deduce the list of ingredients required to prepare a given dish
     * 3. Calls the language model in a loop to order the ingredients using tools
     * 4. Transitions the llm response content to the agent's output (to strategy's nodeFinish)
     *
     * @param tools The CookingAgentTools object that contains the tools for the LLM call.
     */
    fun planAndOrderCookingStrategy(tools: CookingAgentTools): AIAgentStrategy = strategy("advanced-cooking-agent") {

        val nodePlanIngredients by nodeLLMRequest(allowToolCalls = false)

        // Subgraph with task to call llm node with tools:
        // [String -> SubgraphResult]
        val subgraphOrderIngredients by subgraphWithTask<String>(
            tools = tools.asTools(),
            shouldTLDRHistory = false,
        ) { input ->
            // Task for the agent to start ordering
            "Start order ingredients"
        }

        // TODO: Define the agent strategy using the nodes created above, plus [nodeStart] and [nodeFinish]:
        //  [nodeStart] -> [nodePlanIngredients] -> [subgraphOrderIngredients] -> [nodeFinish].
        //  Tips:
        //      1. Use the edge() method declare connection between nodes;
        //      2. Use the forwardTo() method to connect the 'from' node with the 'to' node;
        //      3. Use the onAssistantMessage { message -> true } to handle the condition for transitions between nodes if needed;
        //      4. Use the transformed { subgraphResult -> subgraphResult.result } method
        //         to transform the output from the 'from' node, which is passed to the 'to' node's input.
    }

    //endregion Task 2
}
