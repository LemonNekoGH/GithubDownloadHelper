package moe.nekonest.gdh

import moe.nekonest.gdh.util.ARCHIVE_DIR
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class FileDownloadingHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

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