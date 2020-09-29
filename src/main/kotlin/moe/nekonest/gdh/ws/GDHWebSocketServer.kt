package moe.nekonest.gdh.ws

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import moe.nekonest.gdh.util.*
import org.eclipse.jgit.api.errors.TransportException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.EOFException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/websocket")
@Component
class GDHWebSocketServer : WebSocketServer {
    @OnOpen
    override fun onOpen(session: Session) {
        sessionMap[session.id] = session
        session.attr["connectedTime"] = System.currentTimeMillis()
        onlineNumber++
        logger.info("ID是{}的用户已连接，当前使用人数{}", session.id, onlineNumber)
        session.sendJSON("status" to "connected")
        sessionMap.values.forEach {
            it.sendJSON("online" to onlineNumber.toString())
        }
    }

    @OnClose
    override fun onClose(session: Session) {
        val connectedTime = System.currentTimeMillis() - session.attr["connectedTime"] as Long
        sessionMap.remove(session.id)
        onlineNumber--
        logger.info("ID是{}的用户已断开连接，在线时长{}，当前使用人数{}", session.id, connectedTime, onlineNumber)
        sessionMap.values.forEach {
            it.sendJSON("oneline" to onlineNumber.toString())
        }
    }

    @OnError
    override fun onError(session: Session, error: Throwable) {
        if (error is EOFException) {
            logger.error("ID是${session.id}的用户异常断开")
        } else {
            logger.error("ID是${session.id}的用户的连接状态异常", error)
        }
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
            newDownloadJob(uri).onProgress {
                session.sendStatus(Status.DOWNLOADING, it.toString())
            }.onStart {
                session.sendStatus(Status.DOWNLOADING)
            }.onComplete {
                session.sendStatus(Status.COMPLETED, it)
            }.onError {
                cancel(it.message ?: "未知错误", it)
            }.start()
        }
        session.attr["job"] = job
    }

    private fun doCloneRepository(uri: String, session: Session) {
        val job = GlobalScope.launch {
            newCloneJob(uri).onCompressing {
                session.sendStatus(Status.COMPRESSING)
            }.onStart {
                session.sendStatus(Status.CHECKING_OUT)
            }.onComplete {
                session.sendStatus(Status.COMPLETED, it)
            }.onError {
                when (it) {
                    is TransportException -> {
                        logger.error("仓库不存在")
                        session.sendJSON(
                                "status" to "error",
                                "text" to "仓库不存在"
                        )
                        logger.error("已提示用户并取消任务")
                    }
                    else -> {
                        logger.error(it.message, it)
                    }
                }
                cancel(it.message ?: "未知错误", it)
            }.start()
        }
        session.attr["job"] = job
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        private var onlineNumber = 0

        @JvmStatic
        private val sessionMap = ConcurrentHashMap<String, Session>()

        @JvmStatic
        private val releaseRegex = "https://github.com/[a-zA-Z0-9]+/[a-zA-Z0-9]+/releases/download/.+/.+".toRegex()

        @JvmStatic
        private val codeLoadRegex = "https://github.com/[a-zA-Z0-9]+/[a-zA-Z0-9]+/archive/.+".toRegex()

        @JvmStatic
        private val rawContentRegex = "https://raw.githubusercontent.com/.+".toRegex()

        @JvmStatic
        private val repositoryRegex = "https://github.com/[a-zA-Z0-9]+/[a-zA-Z0-9]+".toRegex()

        @JvmStatic
        private val recaptchaRegex = "token:.+".toRegex()
    }
}