package moe.nekonest.githubproxy.util

import org.apache.logging.log4j.LogManager
import org.eclipse.jgit.api.CloneCommand
import java.io.File

class CloneThread(private val url: String) : Thread(){
    private val logger = LogManager.getLogger()
    var status = Status.READY
    var output = ""

    override fun run() {
        val cc = CloneCommand().setURI(url)
        val dir = File(REPO_DIR,getRepoDirName(url) + "_" + OtherUtil.Date.yyyyMMddHHmmss())
        if (dir.exists()){
            dir.delete()
            logger.info("发现仓库已经存在，正在删除")
        }
        cc.setDirectory(dir)
        status = Status.CHECKING_OUT
        logger.info("开始检出代码")
        cc.call()
        status = Status.COMPRESSING
        logger.info("代码检出完成，开始压缩")
        output = ZipUtil.compress(dir.absolutePath)
        status = Status.COMPLETED
        logger.info("压缩完毕，可以下载")
        dir.delete()
    }

    private fun getRepoDirName(url: String): String{
        val lastIndex = url.lastIndexOf('/')
        return url.slice(lastIndex + 1 .. url.lastIndex)
    }

    enum class Status{
        READY,
        CHECKING_OUT,
        COMPRESSING,
        COMPLETED
    }
}