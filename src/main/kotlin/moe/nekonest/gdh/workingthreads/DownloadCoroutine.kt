package moe.nekonest.gdh.workingthreads

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.nekonest.gdh.util.ARCHIVE_DIR
import moe.nekonest.gdh.util.Size
import org.apache.logging.log4j.LogManager
import java.io.File
import java.net.URI
import kotlin.math.floor

class DownloadCoroutine(private val uri: URI) : WorkingCoroutine() {
    private val logger = LogManager.getLogger()
    private var progress = 0
    private var fullSize = 0L
    private var downloadedSize = 0L
    private var stop = false
    private val fileName = uri.toString().substring(uri.toString().lastIndexOf("/") + 1)

    override lateinit var onStart: () -> Unit
    override lateinit var onComplete: (String) -> Unit
    override lateinit var onError: (Exception) -> Unit
    private lateinit var onProgress: (Int) -> Unit

    fun onProgress(onProgress: (Int) -> Unit): DownloadCoroutine {
        this.onProgress = onProgress
        return this
    }

    override suspend fun run() {
        val downloadDir = File(ARCHIVE_DIR)
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        val fullPathFile = File(downloadDir, fileName)
        val input = uri.toURL().openStream().buffered()
        fullSize = uri.toURL().openConnection().contentLengthLong
        logger.info("内容大小${sizeToString(fullSize)}")
        val out = fullPathFile.outputStream().buffered()
        logger.info("开始下载")
        onStart()
        GlobalScope.launch {
            check()
        }
        var i = input.read()
        while (i != -1) {
            out.write(i)
            downloadedSize++
            i = input.read()
        }
        input.close()
        out.flush()
        out.close()
        stop = true
        logger.info("下载完成")
        onComplete(fileName)
    }

    private fun sizeToString(size: Long): String {
        var convertedSize = 0.0
        val ending = when {
            downloadedSize <= Size.KB -> {
                convertedSize = size.toDouble()
                "B"
            }
            downloadedSize <= Size.MB -> {
                convertedSize = floor((size / Size.KB).toDouble())
                "KB"
            }
            downloadedSize <= Size.GB -> {
                convertedSize = floor((size / Size.MB).toDouble())
                "MB"
            }
            downloadedSize <= Size.TB -> {
                convertedSize = floor((size / Size.GB).toDouble())
                "GB"
            }
            else -> {
                ""
            }
        }
        return "$convertedSize$ending"
    }

    private suspend fun check() {
        while (!stop) {
            progress = ((downloadedSize.toDouble() / fullSize) * 100).toInt()
            logger.info("已下载${sizeToString(downloadedSize)} / ${sizeToString(fullSize)}")
            logger.info("下载进度$progress%")
            onProgress(progress)
            delay(500)
        }
    }
}