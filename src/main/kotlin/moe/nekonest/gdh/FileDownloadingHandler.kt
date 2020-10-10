package moe.nekonest.gdh

import moe.nekonest.gdh.util.ARCHIVE_DIR
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import javax.servlet.http.HttpServletResponse

@Controller
class FileDownloadingHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @RequestMapping("/files/{fileName}")
    fun getFile(@PathVariable fileName: String, response: HttpServletResponse) {
        logger.info("request for download, file name: $fileName")
        response.addHeader("Access-Control-Allow-Origin", "*")
        val fullPathFile = File(ARCHIVE_DIR, fileName)
        if (!fullPathFile.exists()) {
            logger.error("but file not found")
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        } else {
            logger.info("start download")
            response.addHeader("Content-Disposition", "attachment;filename=$fileName")
            response.contentType = "application/octet-stream"
            response.addHeader("Content-Length", fullPathFile.length().toString())
            val out = response.outputStream.buffered()
            out.write(fullPathFile.readBytes())
            out.flush()
            out.close()
            logger.info("download completed")
        }
    }
}