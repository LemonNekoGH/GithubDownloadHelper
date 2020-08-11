package moe.nekonest.gdh

import moe.nekonest.gdh.util.CloneThread
import moe.nekonest.gdh.util.DownloadThread
import moe.nekonest.gdh.util.sendJSON
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

interface WebSocketEndpoint {
    fun onOpen(session: Session)
    fun onClose(session: Session)
    fun onError(session: Session, error: Throwable)
    fun onMessage(session: Session, message: String)
}

@Service
@ServerEndpoint("/checkout")
class MyWebSocketEndpoint : WebSocketEndpoint {
    private val logger = LogManager.getLogger()
    private val sessionMap = HashMap<String, Session>()
    private val threadMap = HashMap<Session, Thread>()

    @OnOpen
    override fun onOpen(session: Session) {
        logger.info("有新的链接被建立了！用户ID是${session.id}")
        sessionMap[session.id] = session
    }

    @OnClose
    override fun onClose(session: Session) {
        logger.info("有一条链接被关闭了！用户ID是${session.id}")
        val thread = threadMap[session]
        if (thread != null) {
            when (thread) {
                is CloneThread -> thread.interrupt()
                is DownloadThread -> thread.interrupt0()
            }
        }
        sessionMap.remove(session.id)
    }

    @OnError
    override fun onError(session: Session, error: Throwable) {
        logger.error("用户ID是${session.id}的链接出现了异常！")
        logger.error("异常信息是${error.message ?: "空的"}")
    }

    @OnMessage
    override fun onMessage(session: Session, message: String) {
        logger.info("ID为${session.id}的用户发来了信息，内容保密~")
        session.sendJSON("status" to "parsing")
        val regex = "[a-zA-z]+://[^\\s]*".toRegex()
        if (message.matches(regex)) {
            if (message.contains("github.com")) {
                if (message.contains("releases") || message.contains("archive")) {
                    logger.info("用户请求下载Release/Archive包")
                    val thread = DownloadThread(message, session)
                    threadMap[session] = thread
                    thread.start()
                } else {
                    logger.info("用户请求检出代码")
                    val thread = CloneThread(message, session)
                    threadMap[session] = thread
                    thread.start()
                }
            } else {
                logger.info("这不是Github链接")
                session.sendJSON(
                        "status" to "error",
                        "text" to "这不是Github链接，你需要另请高明"
                )
            }
        }
    }
}