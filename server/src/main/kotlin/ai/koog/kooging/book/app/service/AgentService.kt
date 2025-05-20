package ai.koog.kooging.book.app.service

import ai.koog.kooging.book.agent.CookingAgent
import ai.koog.kooging.book.app.model.Message
import org.slf4j.LoggerFactory

class AgentService() {

    companion object {
        private val logger = LoggerFactory.getLogger(AgentService::class.java)
    }

    suspend fun startAgent(name: String, cookingRequest: String, onAgentEvent: suspend (Message) -> Unit) {
        val agent = CookingAgent(name)
        logger.info("Starting agent with request: $cookingRequest")
        agent.execute(cookingRequest, onAgentEvent)
    }
}
