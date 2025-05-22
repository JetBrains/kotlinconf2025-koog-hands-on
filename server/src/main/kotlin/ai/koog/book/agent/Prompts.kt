package ai.koog.book.agent

import ai.koog.prompt.markdown.markdown

/**
 * A collection of prompts for the Cooking Agent
 */
object CookingAgentPrompts {

    /**
     * A prompt for the cooking agent to plan the ingredients for cooking a specific dish.
     */
    val planCookingSystemPrompt = markdown {
        h1("GENERAL INSTRUCTIONS")
        bulleted {
            +"You are an agent that helps with cooking"
            +"You must be helpful and respectful"
        }
        h1("TASK")
        bulleted {
            +"You accept the user request, who wants to cook some dish"
            +"Your task is to split the dish into its ingredients"
            +"Output it as a list of bullet-pointed strings in Markdown format"
            +"Ingredient names must be short and concise"
        }

        h1("OUTPUT EXAMPLE")
        bulleted {
            +"Potato"
            +"Beet"
            +"Carrot"
            +"Pickle"
            +"Onion"
            +"Peas"
            +"Oil"
            +"Salt"
        }
    }
}