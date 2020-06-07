package moe.nekonest.githubproxy.util

import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileNotFoundException
import java.lang.IllegalStateException
import java.net.URL
import java.net.UnknownHostException
import javax.websocket.Session
import kotlin.math.floor

class DownloadThread(private val url: String,private val session: Session) : Thread("download-${session.id}") {
    private val progressChecker = Thread(this::check)
    private val logger = LogManager.getLogger()
    private var progress = 0
    private var fullSize = 0L
    private var downloadedSize = 0L
    private var stop = false
    private var interrupt = false
    private val fileName = url.substring(url.lastIndexOf("/") + 1)

    override fun run() {
        try {
            val downloadDir = File(ARCHIVE_DIR)
            if(!downloadDir.exists()){
                downloadDir.mkdirs()
            }
            val fullPathFile = File(downloadDir,fileName)
            val url0 = URL(url)
            val input = url0.openStream().buffered()
            fullSize = url0.openConnection().contentLengthLong
            logger.info("内容大小${sizeToString(fullSize)}")
            val out = fullPathFile.outputStream().buffered()
            logger.info("开始下载")
            session.sendJSON(
                    "status" to "downloading",
                    "text" to "0"
            )
            progressChecker.start()
            var i = input.read()
            while (i != -1 && !interrupt){
                out.write(i)
                downloadedSize ++
                i = input.read()
            }
            input.close()
            out.flush()
            out.close()
            stop = true
            if (!interrupt){
                logger.info("下载完成")
                session.sendJSON(
                        "status" to "completed",
                        "text" to fileName
                )
            }
        }catch (e: IllegalStateException){
            logger.info("用户已离开")
        }catch (e: UnknownHostException){
            logger.info("连接失败")
            session.sendJSON(
                    "status" to "error",
                    "text" to "连接失败，请重试"
            )
        }catch (e: FileNotFoundException){
            logger.info("没找到这个文件")
            session.sendJSON(
                    "status" to "error",
                    "text" to "文件解析失败，请确认链接是否正确"
            )
        }
    }

    fun interrupt0(){
        interrupt = true
        stop = true
        logger.info("中断下载")
        val fullPathFile = File(ARCHIVE_DIR,fileName)
        fullPathFile.delete()
    }

    private fun sizeToString(size: Long): String{
        var convertedSize = 0.0
        val ending = when {
            downloadedSize <= SIZE_KB -> {
                convertedSize = size.toDouble()
                "B"
            }
            downloadedSize <= SIZE_MB -> {
                convertedSize = floor((size / SIZE_KB).toDouble())
                "KB"
            }
            downloadedSize <= SIZE_GB -> {
                convertedSize = floor((size / SIZE_MB).toDouble())
                "MB"
            }
            downloadedSize <= SIZE_TB -> {
                convertedSize = floor((size / SIZE_GB).toDouble())
                "GB"
            }
            else -> {
                ""
            }
        }
        return "$convertedSize$ending"
    }

    private fun check(){
        while (!stop){
            progress = ((downloadedSize.toDouble() / fullSize) * 100).toInt()
            logger.info("已下载${sizeToString(downloadedSize)} / ${sizeToString(fullSize)}")
            logger.info("下载进度$progress%")
            session.sendJSON(
                    "status" to "downloading",
                    "text" to "$progress"
            )
            sleep(1000)
        }
    }
}