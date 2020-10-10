package moe.nekonest.gdh.ws

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import moe.nekonest.gdh.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.EOFException
import java.io.FileNotFoundException
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
        logger.info("connected session with id: ${session.id}, online number: ${SessionMap.onlineNumber()}")
        session.sendJSON("status" to "connected")
        SessionMap.sessions().forEach {
            it.sendJSON("online" to SessionMap.onlineNumber())
        }
    }

    @OnClose
    override fun onClose(session: Session) {
        val connectedTime = System.currentTimeMillis() - SessionMap.getValue(session, "connectedTime") as Long
        val job = SessionMap.getValue(session, "job")
        if (job is Thread) {
            job.interrupt()
        }

        SessionMap.unregister(session)
        logger.info("disconnected session with id: ${session.id}, online time: ${connectedTime}, online number: ${SessionMap.onlineNumber()}")
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
        logger.info("received message from session with id: ${session.id}")
        logger.info(message)

        val jsonObject = JSONObject.parseObject(message)
        val request = jsonObject.getString("request")
            ?: throw Exception("request format error, request not found")
        val url = jsonObject.getString("url")
        val token = jsonObject.getString("token")

        if (request == "keepAlive") {
            logger.info("keep websocket alive.")
            session.sendStatus(Status.KEEP_ALIVE)
            return
        }

        session.sendStatus(Status.PARSING)

        when (request) {
            "download" -> {
                logger.info("ID是${session.id}的用户请求下载文件")
                doDownloadFile(url, session)
            }
            "clone" -> {
                logger.info("ID是${session.id}的用户请求下载仓库")
                doCloneRepository(url, session)
            }
            "check" -> {
                logger.info("ID是${session.id}的用户开始进行验证")
                session.sendStatus(Status.CHECKING)
                val secret = "6LdjY9AZAAAAAFYOcL3znvRS08uQPFCopYGRjW1m"
                val connection =
                    URI.create("https://www.recaptcha.net/recaptcha/api/siteverify?secret=$secret&response=$token")
                        .toURL().openConnection()
                val ret = String(connection.getInputStream().readAllBytes())
                val retObject = JSON.parseObject(ret)
                val success = retObject.getBoolean("success")
                if (success != null && success) {
                    session.sendStatus(Status.CHECKING, "success")
                }
            }
            else -> {
                logger.info("session id: ${session.id}, request format error")
                session.sendStatus(Status.ERROR, "请求格式错误，这是严重错误，请联系柠喵")
            }
        }
    }

    private fun doDownloadFile(uri: String, session: Session) {
        val job = newDownloadJob(uri).onProgress {
            session.sendStatus(Status.DOWNLOADING, it.toString())
        }.onStart {
            session.sendStatus(Status.DOWNLOADING)
        }.onComplete {
            session.sendStatus(Status.COMPLETED, it)
        }.onError { t ->
            logger.error(t.message)
            if (t is FileNotFoundException) {
                session.sendStatus(Status.ERROR, "链接无效，请再检查一次")
            }
        }
        SessionMap.setValue(session, "job", job)
        job.start()
    }

    private fun doCloneRepository(uri: String, session: Session) {
        val workingThread = newCloneJob(uri).onCompressing {
            session.sendStatus(Status.COMPRESSING)
        }.onStart {
            session.sendStatus(Status.CHECKING_OUT)
        }.onComplete {
            session.sendStatus(Status.COMPLETED, it)
        }.onProgress {
            logger.info("clone progress: $it%")
            session.sendStatus(Status.CHECKING_OUT, it.toString())
        }.onError {
            logger.error(it.message)
            if (it.message == "remote authentication required but no callback set") {
                session.sendStatus(Status.ERROR, "仓库不存在或仓库是私有仓库")
            }
        }
        val job = workingThread.start()
        SessionMap.setValue(session, "job", job)
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}