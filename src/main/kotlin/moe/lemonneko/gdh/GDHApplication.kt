package moe.lemonneko.gdh

import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import moe.lemonneko.nekogit.NekoGit
import moe.lemonneko.nekogit.cmds.CloneCommand
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.set
import kotlin.concurrent.thread
import kotlin.math.floor

private val logger = LoggerFactory.getLogger("GDHApplicationKt")
private val sessions = ArrayList<DefaultWebSocketServerSession>()

private val repoDir = System.getProperty("user.home") + File.separator + "repos"
private val archiveDir = System.getProperty("user.home") + File.separator + "archives"

private val httpClient = HttpClient(OkHttp)

fun main() {
    NekoGit.init()
    embeddedServer(
        Netty,
        port = 4000,
        module = Application::mainModule
    ).start(wait = true)
    NekoGit.destroy()
}

fun Application.mainModule() {
    install(WebSockets)
    install(AutoHeadResponse)
    routing {
        webSocket()
        files()
    }
}

fun Routing.webSocket() {
    webSocket("/websocket") {
        logger.info("websocket connected.")
        if (!sessions.contains(this)) {
            sessions.add(this)
            logger.info("added this session to session list.")
            sessions.forEach {
                it.sendJson(listOf("online" to sessions.size.toString()))
            }
        }
        try {
            var count = 0
            for (frame in incoming) {
                count++
                logger.info("received message count: $count")
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    logger.info("received message: $text")
                    try {
                        sendStatus(Status.PARSING)
                        val jsonObject = JSONObject.parseObject(text)
                        val request = jsonObject.getString("request")
                            ?: throw BadRequestException("request format error, request not found")
                        val url = jsonObject.getString("url")
                        val token = jsonObject.getString("token")
                        when (request) {
                            "check" -> {
                                token ?: throw BadRequestException("request format error, token not found")
                                doCheck(token)
                            }
                            "download" -> {
                                url ?: throw BadRequestException("request format error, url not found")
                                doDownload(url)
                            }
                            "clone" -> {
                                url ?: throw BadRequestException("request format error, url not found")
                                doClone(url)
                            }
                            else -> throw BadRequestException("request method error: $request")
                        }
                    } catch (e: JSONException) {
                        logger.error("error: ${e.message}", e)
                        sendStatus(Status.ERROR, "请求不是JSON，这是严重错误，请联系柠喵")
                    } catch (e: NullPointerException) {
                        logger.error("error: ${e.message}", e)
                        sendStatus(Status.ERROR, "请求格式错误，这是严重错误，请联系柠喵")
                    } catch (e: SocketTimeoutException) {
                        logger.error("error: ${e.message}", e)
                        sendStatus(Status.ERROR, "Github没有响应，请重试")
                    }
                }
                logger.info("process done")
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.info("session closed with: ${closeReason.await()}")
        } catch (e: Throwable) {
            logger.error("session error with: ${closeReason.await()}", e)
        }
        logger.info("session closed.")
        if (sessions.contains(this)) {
            sessions.remove(this)
            logger.info("removed this session from session list.")
            sessions.forEach {
                it.sendJson(listOf("online" to sessions.size.toString()))
            }
        }
    }
}

private fun DefaultWebSocketServerSession.doCheck(token: String) {
    launch {
        val request =
            httpClient.post<String>("https://www.recaptcha.net/recaptcha/api/siteverify?secret=6LdjY9AZAAAAAFYOcL3znvRS08uQPFCopYGRjW1m&response=$token")
        val jsonObject = JSONObject.parseObject(request)
        val success = jsonObject.getBoolean("success")
        if (success) {
            sendStatus(Status.CHECKING, "success")
        }
    }
}

private suspend fun DefaultWebSocketServerSession.doDownload(url: String) {
    logger.info("do download, url=$url")
    val fileName = url.substring(url.lastIndexOf('/') + 1)
    logger.info("file name: $fileName")
    val file = File(archiveDir, fileName)

    thread {
        launch {
            sendStatus(Status.DOWNLOADING, "0")
        }
        saveFileFromURL(url, file) {
            launch {
                sendStatus(Status.DOWNLOADING, it.toString())
                logger.info("progress: $it")
            }
        }
        launch {
            sendStatus(Status.COMPLETED, fileName)
            logger.info("done")
        }
    }
}

private fun saveFileFromURL(url: String, file: File, progress: (Int) -> Unit) {
    val connection = URL(url).openConnection()
    connection.connectTimeout = 5000
    val contentLength = connection.contentLength.toFloat()
    logger.info("connected, content length: $contentLength")
    var i = 0F

    if (!file.exists()) {
        file.createNewFile()
    }

    val out = BufferedOutputStream(FileOutputStream(file))

    val input = connection.getInputStream().buffered()
    var byte: Int
    do {
        byte = input.read()
        out.write(byte)
        i++
        if (contentLength > 0 && (i % (1024 * 64)) == 0F) {
            progress(floor((i / contentLength) * 100).toInt())
        }
    } while (byte != -1)

    out.flush()
    out.close()
    input.close()
}

private fun DefaultWebSocketServerSession.doClone(url: String) {
    logger.info("do clone, url=$url")
    launch {
        sendStatus(Status.CHECKING_OUT, "0")
    }
    val repo = File(repoDir, url.substring(url.lastIndexOf('/') + 1))

    if (repo.exists()) {
        logger.warn("repo exists, deleting...")
        Runtime.getRuntime().exec("rm -rf " + repo.absolutePath)
    }

    CloneCommand()
        .url(url)
        .path(repo.absolutePath)
        .onProgress {
            if (it != 100) {
                launch {
                    sendStatus(Status.CHECKING_OUT, it.toString())
                }
            } else {
                try {
                    val output = doZip(repo)
                    launch {
                        sendStatus(Status.COMPLETED, output)
                    }
                    Runtime.getRuntime().exec("rm -rf " + repo.absolutePath)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }.onError {
            Runtime.getRuntime().exec("rm -rf " + repo.absolutePath)
            launch {
                sendStatus(Status.ERROR, "请检查仓库路径后重试")
            }
        }.call()
}

fun Routing.files() {
    static("/files") {
        staticRootFolder = File(System.getProperty("user.home") + "/archives")
        files(".")
    }
}

private fun doZip(file: File): String {
    val archiveDir = File(archiveDir)
    if (!archiveDir.exists()) {
        archiveDir.mkdirs()
    }
    val outputFile = File(archiveDir, file.name + ".zip")
    if (!outputFile.exists()) {
        outputFile.createNewFile()
    }

    val zout = ZipOutputStream(FileOutputStream(outputFile))
    val bout = zout.buffered()

    doZip0(file, zout, bout, "")

    bout.close()
    zout.close()
    return outputFile.name
}

private fun doZip0(file: File, zout: ZipOutputStream, bout: BufferedOutputStream, path: String) {
    println("compressing: ${path + File.separator + file.name}")
    if (file.isDirectory) {
        file.listFiles()?.forEach {
            doZip0(it, zout, bout, path + File.separator + file.name)
        }
    } else {
        val zipEntry = ZipEntry(path + File.separator + file.name)
        zout.putNextEntry(zipEntry)
        bout.write(file.readBytes())
        bout.flush()
    }
    zout.closeEntry()
}

private suspend fun DefaultWebSocketServerSession.sendJson(messagePairs: List<Pair<String, String>>) {
    val jsonObject = JSONObject()
    messagePairs.forEach {
        jsonObject[it.first] = it.second
    }
    send(Frame.Text(jsonObject.toJSONString()))
}

private suspend fun DefaultWebSocketServerSession.sendStatus(status: Status, text: String = "") {
    sendJson(
        listOf(
            "status" to status.value,
            "text" to text
        )
    )
}

private enum class Status(val value: String) {
    PARSING("parsing"),
    CHECKING_OUT("checking out"),
    DOWNLOADING("downloading"),
    CHECKING("checking"),
    ERROR("error"),
    COMPLETED("completed")
}