package moe.nekonest.githubproxy

import moe.nekonest.githubproxy.util.CloneThread
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

interface WebSocketEndpoint{
    fun onOpen(session: Session)
    fun onClose(session: Session)
    fun onError(session: Session,error: Throwable)
    fun onMessage(session: Session, message: String)
}

@Service
@ServerEndpoint("/checkout")
class MyWebSocketEndpoint : WebSocketEndpoint {
    private val logger = LogManager.getLogger()
    private val sessionMap = HashMap<String, Session>()
    @OnOpen
    override fun onOpen(session: Session) {
        logger.info("有新的链接被建立了！用户ID是${session.id}")
        sessionMap[session.id] = session
    }

    @OnClose
    override fun onClose(session: Session) {
        logger.info("有一条链接被关闭了！用户ID是${session.id}")
        sessionMap.remove(session.id)
    }

    @OnError
    override fun onError(session: Session,error: Throwable) {
        logger.error("用户ID是${session.id}的链接出现了异常！")
        logger.error("异常信息是${error.message ?: "空的"}")
    }

    @OnMessage
    override fun onMessage(session: Session, message: String) {
        logger.info("ID为${session.id}的用户发来了信息，内容保密~")
        val regex = "[a-zA-z]+://[^\\s]*".toRegex()
        if (message.matches(regex)){
            logger.info("请求Checkout")
            CloneThread(message,session).start()
        }
    }

}