package moe.nekonest.gdh.workingthreads

import kotlinx.coroutines.delay
import moe.nekonest.gdh.util.REPO_DIR
import moe.nekonest.gdh.util.ZipUtil
import moe.nekonest.gdh.util.sendJSON
import org.eclipse.jgit.api.CloneCommand
import org.slf4j.LoggerFactory
import java.io.File
import javax.websocket.Session

class CloneCoroutine(
        private val uri: String,
        private val session: Session
) : WorkingCoroutine {
    private val logger = LoggerFactory.getLogger(this::class.java)
    var output = ""

    override suspend fun run() {
        val repoDir = File(REPO_DIR, getRepoName(uri))
        if (!repoDir.exists()) {
            logger.info("仓库不存在，需要检出")
            val cloneCommand = CloneCommand().setURI(uri).setDirectory(repoDir)
            logger.info("开始检出代码")
            session.sendJSON(
                    "status" to "checking out"
            )
            cloneCommand.call()
            logger.info("检出完成")
        }
        output = ZipUtil.compress(repoDir.absolutePath)
        delay(1000)
        logger.info("压缩完毕，可以下载")
        session.sendJSON(
                "status" to "completed",
                "text" to output
        )
    }

    private fun getRepoName(uri: String): String {
        var lastIndex = uri.lastIndexOf('/')
        return uri.slice(++lastIndex..uri.lastIndex)
    }
}