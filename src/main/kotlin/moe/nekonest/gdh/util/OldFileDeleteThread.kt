package moe.nekonest.gdh.util

import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class OldFileDeleteThread(private val sleepTime: Long, private val fileTimedOut: Long) : Thread() {
    private val logger = LogManager.getLogger()
    override fun run() {
        while (true){
            val repoDir = File(REPO_DIR)
            System.gc()
            repoDir.listFiles()?.forEach(this::checkFileTimeAndDelete)
            val archiveDir = File(ARCHIVE_DIR)
            System.gc()
            archiveDir.listFiles()?.forEach(this::checkFileTimeAndDelete)
            sleep(sleepTime)
        }
    }

    private fun checkFileTimeAndDelete(file: File){
        val time = file.lastModified()
        if (System.currentTimeMillis() - time > fileTimedOut){
            logger.info("正在删除已过期文件（夹）：${file.name}")
            doDelete(file)
        }
    }

    private fun doDelete(file: File){
        if (file.isDirectory){
            file.listFiles()?.forEach(this::doDelete)
        }else{
            Files.delete(Paths.get(file.toURI()))
        }
    }
}