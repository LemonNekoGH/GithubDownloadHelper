package moe.nekonest.gdh.workingthreads

import moe.nekonest.gdh.util.ARCHIVE_DIR
import moe.nekonest.gdh.util.Size
import org.apache.logging.log4j.LogManager
import java.io.File
import java.net.URL
import kotlin.math.floor

class DownloadThread(private val uri: String) : WorkingThread() {
    private val logger = LogManager.getLogger()
    private var fullSize = 0L
    private var downloadedSize = 0L
    private var stop = false
    private val fileName = uri.substring(uri.lastIndexOf("/") + 1)

    override lateinit var onStart: () -> Unit
    override lateinit var onComplete: (String) -> Unit
    override lateinit var onError: WorkingThread.(Throwable) -> Unit
    override lateinit var onProgress: (Int) -> Unit

    override fun run() {
        val downloadDir = File(ARCHIVE_DIR)
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        val fullPathFile = File(downloadDir, fileName)
        val connection = URL(uri).openConnection()
        fullSize = connection.contentLengthLong
        val input = connection.getInputStream()
        logger.info("content length${sizeToString(fullSize)}")
        val out = fullPathFile.outputStream().buffered()
        logger.info("do download")
        onStart()
        var i = input.read()
        while (i != -1) {
            out.write(i)
            downloadedSize++
            if (downloadedSize % (64 * Size.KB) == 0L) {
                onProgress((downloadedSize / fullSize).toInt())
            }
            i = input.read()
        }
        input.close()
        out.flush()
        out.close()
        stop = true
        logger.info("download completed")
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
}