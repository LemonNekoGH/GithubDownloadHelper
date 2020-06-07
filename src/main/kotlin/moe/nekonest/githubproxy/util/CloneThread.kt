package moe.nekonest.githubproxy.util

import org.apache.logging.log4j.LogManager
import org.eclipse.jgit.api.CloneCommand
import java.io.File
import javax.websocket.Session

class CloneThread(private val url: String, private val session: Session) : Thread(){
    private val logger = LogManager.getLogger()
    var output = ""

    override fun run() {
        try {
            val cc = CloneCommand().setURI(url)
            val dir = File(REPO_DIR,getRepoDirName(url) + "_" + OtherUtil.Date.yyyyMMddHHmmss())
            if (dir.exists()){
                dir.delete()
                logger.info("发现仓库已经存在，正在删除")
            }
            cc.setDirectory(dir)
            logger.info("开始检出代码")
            session.sendJSON("status" to "checking out")
            cc.call()
            logger.info("代码检出完成，开始压缩")
            session.sendJSON("status" to "compressing")
            output = ZipUtil.compress(dir.absolutePath)
            sleep(1000)
            logger.info("压缩完毕，可以下载")
            session.sendJSON("status" to "completed","text" to output)
            dir.delete()
        }catch (e: IllegalStateException){
            logger.warn("用户已离开")
        }
    }

    private fun getRepoDirName(url: String): String{
        val lastIndex = url.lastIndexOf('/')
        return url.slice(lastIndex + 1 .. url.lastIndex)
    }
}