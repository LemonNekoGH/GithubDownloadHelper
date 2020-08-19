package moe.nekonest.gdh.ws

import moe.nekonest.gdh.util.sendJSON
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/websocket")
@Component
class GDHWebSocketServer : WebSocketServer {
    @OnOpen
    override fun onOpen(session: WebSocketSession) {
        sessionMap[session.id] = session
        onlineNumber++
        logger.info("ID是{}的用户已连接，当前使用人数{}", session.id, onlineNumber)
        session.sendJSON("status" to "connected")
        sessionMap.values.forEach {
            it.sendJSON("online" to onlineNumber.toString())
        }
    }

    @OnClose
    override fun onClose(session: WebSocketSession) {
        sessionMap.remove(session.id)
        onlineNumber--
        logger.info("ID是{}的用户已断开连接，当前使用人数{}", session.id, onlineNumber)
        sessionMap.values.forEach {
            it.sendJSON("oneline" to onlineNumber.toString())
        }
    }

    @OnError
    override fun onError(session: WebSocketSession, error: Throwable) {
        logger.error("ID是${session.id}的用户的连接状态异常", error)
    }

    @OnMessage
    override fun onMessage(session: WebSocketSession, message: String) {
        logger.info("ID是${session.id}的用户发来了请求，正在解析请求")
        session.sendJSON("status" to "parsing")

        when {
            message.matches(releaseRegex) -> {
                logger.info("ID是${session.id}的用户请求下载release包")
                doDownloadFile(URI.create(message), session)
            }
            message.matches(codeLoadRegex) -> {
                logger.info("ID是${session.id}的用户请求下载源码包")
                doDownloadFile(URI.create(message), session)
            }
            message.matches(rawContentRegex) -> {
                logger.info("ID是${session.id}的用户请求下载原始文件")
                doDownloadFile(URI.create(message), session)
            }
            message.matches(repositoryRegex) -> {
                logger.info("ID是${session.id}的用户请求下载仓库")
                doCloneRepository(message, session)
            }
        }
    }

    private fun doDownloadFile(uri: URI, session: WebSocketSession) {

    }

    private fun doCloneRepository(uri: String, session: WebSocketSession) {

    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        private var onlineNumber = 0

        @JvmStatic
        private val sessionMap = ConcurrentHashMap<String, WebSocketSession>()

        @JvmStatic
        private val releaseRegex = "https://github.com/\\w+/\\w+/releases/download/[\\w\\W]+/[\\w\\W]+".toRegex()

        @JvmStatic
        private val codeLoadRegex = "https://codeload.github.com/\\w+/\\w+/zip/master".toRegex()

        @JvmStatic
        private val rawContentRegex = "https://raw.githubusercontent.com/[\\w+\\W+]+".toRegex()

        @JvmStatic
        private val repositoryRegex = "https://github.com/\\w+/\\w+/".toRegex()
    }
}