package moe.nekonest.gdh.workingthreads

import moe.nekonest.gdh.util.REPO_DIR
import moe.nekonest.gdh.util.ZipUtil
import moe.nekonest.gdh.util.sendJSON
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.lib.BranchConfig
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketSession
import java.io.File

class CloneThread(
        private val uri: String,
        private val session: WebSocketSession
) : Thread("CloneThread-${session.id}") {
    private val logger = LoggerFactory.getLogger(this::class.java)
    var output = ""

    override fun run() {
        try {
            val repoDir = File(REPO_DIR, getRepoName(uri))
            if (repoDir.exists()) {
                logger.info("发现仓库已存在，正在拉取最新数据")
                val repo = RepositoryBuilder()
                        .setGitDir(repoDir)
                        .readEnvironment()
                        .findGitDir()
                        .build()
                val pullCommandConstructor = PullCommand::class.java
                        .getDeclaredConstructor(Repository::class.java)
                pullCommandConstructor.isAccessible = true
                val pullCommand = pullCommandConstructor.newInstance(repo) as PullCommand
                pullCommand.setRebase(BranchConfig.BranchRebaseMode.REBASE)
                session.sendJSON(
                        "status" to "checking out"
                )
                pullCommand.call()
                logger.info("拉取完毕")
                output = ZipUtil.compress(repoDir.absolutePath)
                sleep(1000)
                logger.info("压缩完毕，可以下载")
                session.sendJSON(
                        "status" to "completed",
                        "text" to output
                )
            } else {
                logger.info("仓库不存在，需要检出")
                val cloneCommand = CloneCommand().setURI(uri).setDirectory(repoDir)
                logger.info("开始检出代码")
                session.sendJSON(
                        "status" to "checking out"
                )
                cloneCommand.call()
                logger.info("检出完成")
                output = ZipUtil.compress(repoDir.absolutePath)
                sleep(1000)
                logger.info("压缩完毕，可以下载")
                session.sendJSON(
                        "status" to "completed",
                        "text" to output
                )
            }
        } catch (e: IllegalStateException) {
            logger.error(e.message, e)
        }
    }

    private fun getRepoName(uri: String): String {
        var lastIndex = uri.lastIndexOf('/')
        return uri.slice(++lastIndex..uri.lastIndex)
    }
}