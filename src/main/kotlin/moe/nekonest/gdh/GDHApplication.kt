package moe.nekonest.gdh

import moe.nekonest.gdh.util.ARCHIVE_DIR
import org.apache.logging.log4j.LogManager
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import java.io.PrintStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// 本文件的全局Logger
private val logger = LogManager.getLogger()

@SpringBootApplication
class GDHApplication {
    object GDHBanner : Banner {
        override fun printBanner(environment: Environment?, sourceClass: Class<*>?, out: PrintStream?) {
            out ?: throw IllegalArgumentException("param 'out' is null!")
            out.println("=========================================================")
            out.println("|                                                       |")
            out.println("|      GGGGGGGGGG      DDDDDDDDDD      HHH      HHH     |")
            out.println("|     GGG              DDD     DDD     HHH      HHH     |")
            out.println("|    GGG     GGGGGG    DDD      DDD    HHHHHHHHHHHH     |")
            out.println("|     GGG      GGG     DDD     DDD     HHH      HHH     |")
            out.println("|      GGGGGGGGGG      DDDDDDDDDD      HHH      HHH     |")
            out.println("|                                                       |")
            out.println("|   - Help You To Downloading Resources From Github -   |")
            out.println("|                                                       |")
            out.println("=========================================================")
        }
    }
}

@Controller
class MainController {

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
            response.addHeader("Content-Length", fullPathFile.length().toString())
            val out = response.outputStream.buffered()
            out.write(fullPathFile.readBytes())
            out.flush()
            out.close()
            logger.info("下载结束")
        }
    }
}

fun main(args: Array<String>) {
    val gdhApplication = SpringApplication(GDHApplication::class.java)
    gdhApplication.setBanner(GDHApplication.GDHBanner)
    gdhApplication.run(*args)
}