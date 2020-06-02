package moe.nekonest.githubproxy

import moe.nekonest.githubproxy.util.ARCHIVE_DIR
import moe.nekonest.githubproxy.util.CloneThread
import moe.nekonest.githubproxy.util.compareTo
import org.apache.logging.log4j.LogManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.server.Session
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.math.log

@SpringBootApplication
class GithubProxyApplication

@Controller
class MainController{
    private val logger = LogManager.getLogger()

    /**
     *  检查当前会话是否已经在检出代码了
     *  是：返回状态，如果已经完成就放出下载地址
     *  否：新建线程用于检出代码，并将这个线程放进会话属性中
     */
    @RequestMapping("/checkout")
    fun getUrl(url: String, request: HttpServletRequest,response: HttpServletResponse){
        response.addHeader("Access-Control-Allow-Origin","*")
        val out = response.outputStream
        logger.info("收到请求，url: $url")
        val session = request.session
        if(session == null){
            "{\"status\": \"failed\"}" > out
        }
        var thread = session.getAttribute("thread") as CloneThread?
        if (thread == null){
            logger.info("新建会话")
            thread = CloneThread(url)
            thread.start()
            session.setAttribute("thread",thread)
            "{\"status\": \"checking out\"}" > out
        }else{
            logger.info("返回已有会话状态：${thread.status}")
            when(thread.status){
                CloneThread.Status.READY -> "" > out
                CloneThread.Status.CHECKING_OUT -> "{\"status\": \"checking out\"}" > out
                CloneThread.Status.COMPRESSING -> "{\"status\": \"compressing\"}" > out
                CloneThread.Status.COMPLETED ->
                    "{\"status\": \"completed\",\"fileName\": \"${thread.output}\"}" > out
            }
        }
    }

    @RequestMapping("/file")
    fun getFile(fileName: String,request: HttpServletRequest,response: HttpServletResponse){
        logger.info("收到下载请求，文件名是：$fileName")
        response.addHeader("Access-Control-Allow-Origin","*")
        val fullPathFile = File(ARCHIVE_DIR,fileName)
        if (!fullPathFile.exists()){
            logger.error("文件不存在")
            response.outputStream.write("{\"status\": \"failed\", \"errorCode\": \"404\"}".toByteArray())
        }else{
            logger.info("开始下载")
            response.contentType = "application/octet-stream"
            val out = response.outputStream.buffered()
            out.write(fullPathFile.readBytes())
            out.flush()
            out.close()
            logger.info("下载结束")
        }
        if (request.session.getAttribute("thread") != null){
            request.session.removeAttribute("thread")
            logger.info("已删除会话")
        }
    }

    @RequestMapping("/getBuildGradle")
    fun getBuildGradle(response: HttpServletResponse,request: HttpServletRequest){
        val url = "./build.gradle.kts"
        val file = File(url)
        response.contentType = "application/force-download"
        response.addHeader("Content-Disposition","attachment;fileName=build.gradle.kts")
        val out = response.outputStream
        val bufferedOut = out.buffered()
        bufferedOut.write(file.readBytes())
        bufferedOut.close()
    }
}

fun main(args: Array<String>) {
    runApplication<GithubProxyApplication>(*args)
}
