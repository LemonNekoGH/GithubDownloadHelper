package moe.nekonest.gdh.ws

import com.alibaba.fastjson.JSON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import moe.nekonest.gdh.util.*
import moe.nekonest.gdh.workingthreads.CloneCoroutine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.EOFException
import java.net.URI
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/websocket")
@Component
class GDHWebSocketServer : WebSocketServer {
    @OnOpen
    override fun onOpen(session: Session) {
        SessionMap.register(session)
        SessionMap.setValue(session, "id", session.id)
        SessionMap.setValue(session, "connectedTime", System.currentTimeMillis())
        logger.info("ID是{}的用户已连接，当前使用人数{}", session.id, SessionMap.onlineNumber())
        session.sendJSON("status" to "connected")
        SessionMap.sessions().forEach {
            it.sendJSON("online" to SessionMap.onlineNumber())
        }
    }

    @OnClose
    override fun onClose(session: Session) {
        val connectedTime = System.currentTimeMillis() - SessionMap.getValue(session, "connectedTime") as Long
        val job = SessionMap.getValue(session, "job")
        if (job is Job) {
            job.cancel()
        }

        val coroutine = SessionMap.getValue(session, "coroutine")
        if (coroutine is CloneCoroutine) {
            coroutine.destroy()
        }

        SessionMap.unregister(session)
        logger.info("ID是{}的用户已断开连接，在线时长{}，当前使用人数{}", session.id, connectedTime, SessionMap.onlineNumber())
        SessionMap.sessions().forEach {
            it.sendJSON("oneline" to SessionMap.onlineNumber())
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
            val coroutine = newDownloadJob(uri).onProgress {
                session.sendStatus(Status.DOWNLOADING, it.toString())
            }.onStart {
                session.sendStatus(Status.DOWNLOADING)
            }.onComplete {
                session.sendStatus(Status.COMPLETED, it)
            }.onError {
                cancel(it.message ?: "未知错误", it)
            }
            SessionMap.setValue(session, "coroutine", coroutine)
            coroutine.start()
        }
        SessionMap.setValue(session, "job", job)
    }

    private fun doCloneRepository(uri: String, session: Session) {
        val coroutine = newCloneJob(uri).onCompressing {
            session.sendStatus(Status.COMPRESSING)
        }.onStart {
            session.sendStatus(Status.CHECKING_OUT)
        }.onComplete {
            session.sendStatus(Status.COMPLETED, it)
        }.onProgress {
            logger.info("clone进度$it%")
            session.sendStatus(Status.CHECKING_OUT, it.toString())
        }.onError {
            logger.error(it.message)
            cancel()
        }
        SessionMap.setValue(session, "coroutine", coroutine)
        val job = coroutine.start()
        SessionMap.setValue(session, "job", job)
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        private val releaseRegex = "https://github.com/[a-zA-Z0-9]+/[a-zA-Z0-9\\-]+/releases/download/.+/.+".toRegex()

        @JvmStatic
        private val codeLoadRegex = "https://github.com/[a-zA-Z0-9]+/[a-zA-Z0-9\\-]+/archive/.+".toRegex()

        @JvmStatic
        private val rawContentRegex = "https://raw.githubusercontent.com/.+".toRegex()

        @JvmStatic
        private val repositoryRegex = "https://github.com/[a-zA-Z0-9]+/[a-zA-Z0-9\\-]+(.git)?".toRegex()

        @JvmStatic
        private val recaptchaRegex = "token:.+".toRegex()
    }
}