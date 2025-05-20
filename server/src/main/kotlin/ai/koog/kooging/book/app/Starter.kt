package ai.koog.kooging.book.app

import ai.koog.kooging.book.app.server.KoogBookServer
import ai.koog.kooging.book.app.server.KoogServerConfig
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.net.ServerSocket

private val logger = LoggerFactory.getLogger("KoogBookServer")

fun main() = runBlocking{

    val availablePort = findAvailablePort()
    val serverConfig = KoogServerConfig(port = availablePort)
    KoogBookServer(config = serverConfig).use { server ->
        logger.info("----- Starting server on port: $availablePort -----")
        server.startServer(wait = true)
        logger.info("----- Server is finished -----")
    }
}

//region Private Methods

private fun findAvailablePort(): Int {
    val port = ServerSocket(0).use { socket ->
        socket.localPort
    }

    return port
}

//endregion Private Methods