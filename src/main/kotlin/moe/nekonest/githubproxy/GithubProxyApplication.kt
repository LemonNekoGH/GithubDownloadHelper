package moe.nekonest.githubproxy

import moe.nekonest.githubproxy.util.ARCHIVE_DIR
import moe.nekonest.githubproxy.util.CloneThread
import moe.nekonest.githubproxy.util.compareTo
import moe.nekonest.githubproxy.util.getAttribute
import org.apache.logging.log4j.LogManager
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.websocket.OnClose
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint

@SpringBootApplication
class GithubProxyApplication

@Controller
class MainController {
    private val logger = LogManager.getLogger()

    @RequestMapping("/file")
    fun getFile(fileName: String, request: HttpServletRequest, response: HttpServletResponse) {
        logger.info("收到下载请求，文件名是：$fileName")
        response.addHeader("Access-Control-Allow-Origin", "*")
        val fullPathFile = File(ARCHIVE_DIR, fileName)
        if (!fullPathFile.exists()) {
            logger.error("文件不存在")
            response.outputStream.write("{\"status\": \"failed\", \"errorCode\": \"404\"}".toByteArray())
        } else {
            logger.info("开始下载")
            response.addHeader("Content-Disposition", "attachment;filename=$fileName")
            response.contentType = "application/octet-stream"
            val out = response.outputStream.buffered()
            out.write(fullPathFile.readBytes())
            out.flush()
            out.close()
            logger.info("下载结束")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<GithubProxyApplication>(*args)
}
