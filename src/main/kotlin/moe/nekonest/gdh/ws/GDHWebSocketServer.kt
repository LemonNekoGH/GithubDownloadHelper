package moe.nekonest.gdh.ws

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moe.nekonest.gdh.util.Status
import moe.nekonest.gdh.util.sendJSON
import moe.nekonest.gdh.util.sendStatus
import moe.nekonest.gdh.workingthreads.CloneCoroutine
import moe.nekonest.gdh.workingthreads.DownloadCoroutine
import org.eclipse.jgit.api.errors.TransportException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/websocket")
@Component
class GDHWebSocketServer : WebSocketServer {
    private val jobMap = HashMap<Session, Job>()

    @OnOpen
    override fun onOpen(session: Session) {
        sessionMap[session.id] = session
        onlineNumber++
        logger.info("ID是{}的用户已连接，当前使用人数{}", session.id, onlineNumber)
        session.sendJSON("status" to "connected")
        sessionMap.values.forEach {
            it.sendJSON("online" to onlineNumber.toString())
        }
    }

    @OnClose
    override fun onClose(session: Session) {
        sessionMap.remove(session.id)
        onlineNumber--
        logger.info("ID是{}的用户已断开连接，当前使用人数{}", session.id, onlineNumber)
        sessionMap.values.forEach {
            it.sendJSON("oneline" to onlineNumber.toString())
        }
    }

    @OnError
    override fun onError(session: Session, error: Throwable) {
        logger.error("ID是${session.id}的用户的连接状态异常", error)
    }

    @OnMessage
    override fun onMessage(message: String, session: Session) {
        logger.info("ID是${session.id}的用户发来了请求，正在解析请求")
        session.sendStatus(Status.PARSING)

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
            message.matches(recaptchaRegex) -> {
                logger.info("ID是${session.id}的用户开始进行验证")
                session.sendStatus(Status.CHECKING)
                val secret = "6LdjY9AZAAAAAFYOcL3znvRS08uQPFCopYGRjW1m"
                val response = message.substring(message.lastIndexOf(':') + 1)
                val connection =
                        URI.create("https://www.recaptcha.net/recaptcha/api/siteverify?secret=$secret&response=$response")
                                .toURL().openConnection()
                val ret = String(connection.getInputStream().readAllBytes())
                logger.info("返回：$ret")
                val retObject = JSON.parseObject(ret)
                val success = retObject.getBoolean("success")
                if (success != null && success) {
                    session.sendStatus(Status.CHECKING, "success")
                }
            }
            else -> {
                logger.info("ID是${session.id}的用户传来的参数格式错误")
                session.sendStatus(Status.ERROR, "格式错误，如果是仓库地址则需要加.git后缀")
            }
        }
    }

    private fun doDownloadFile(uri: URI, session: Session) {
        val job = GlobalScope.launch {
            DownloadCoroutine(uri, session).run()
        }
        jobMap[session] = job
    }

    private fun doCloneRepository(uri: String, session: Session) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            when (throwable) {
                is TransportException -> {
                    logger.error("仓库不存在")
                    session.sendJSON(
                            "status" to "error",
                            "text" to "仓库不存在"
                    )
                    logger.error("已提示用户并取消任务")
                }
                else -> {
                    logger.error(throwable.message, throwable)
                }
            }
        }
        val job = GlobalScope.launch(exceptionHandler) {
            logger.info("任务已启动")
            CloneCoroutine(uri, session).run()
        }
        jobMap[session] = job
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        private var onlineNumber = 0

        @JvmStatic
        private val sessionMap = ConcurrentHashMap<String, Session>()

        @JvmStatic
        private val releaseRegex = "https://github.com/.+/.+/releases/download/.+/.+".toRegex()

        @JvmStatic
        private val codeLoadRegex = "https://github.com/.+/.+/archive/.+".toRegex()

        @JvmStatic
        private val rawContentRegex = "https://raw.githubusercontent.com/.+".toRegex()

        @JvmStatic
        private val repositoryRegex = "https://github.com/.+.git".toRegex()

        @JvmStatic
        private val recaptchaRegex = "token:.+".toRegex()
    }
}