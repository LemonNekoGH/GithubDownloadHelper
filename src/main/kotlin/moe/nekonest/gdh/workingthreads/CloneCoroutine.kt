package moe.nekonest.gdh.workingthreads

import kotlinx.coroutines.delay
import moe.lemonneko.nekogit.cmds.CloneCommand
import moe.nekonest.gdh.util.REPO_DIR
import moe.nekonest.gdh.util.ZipUtil
import org.slf4j.LoggerFactory
import java.io.File

class CloneCoroutine(private val uri: String) : WorkingCoroutine() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var output = ""
    private val repoDir = File(REPO_DIR, getRepoName(uri))
    override lateinit var onStart: () -> Unit
    override lateinit var onComplete: (String) -> Unit
    override lateinit var onError: WorkingCoroutine.(Exception) -> Unit
    override lateinit var onProgress: (Int) -> Unit

    private lateinit var onCompressing: () -> Unit

    fun onCompressing(onCompressing: () -> Unit): CloneCoroutine {
        this.onCompressing = onCompressing
        return this
    }

    override suspend fun run() {
        if (!repoDir.exists()) {
            val cloneCommand = CloneCommand()
                    .url(uri)
                    .path(repoDir.absolutePath)
                    .onProgress {
                        onProgress(it)
                        if (it == 100) {
                            logger.info("检出完成")
                            output = ZipUtil.compress(repoDir.absolutePath)
                            logger.info("压缩完毕，可以下载")
                            onComplete(output)
                        }
                    }
                    .onError {
                        onError(it)
                    }
            logger.info("开始检出代码")
            onStart()
            cloneCommand.call().join()
        } else {
            output = ZipUtil.compress(repoDir.absolutePath)
            delay(1000)
            logger.info("压缩完毕，可以下载")
            onComplete(output)
        }
    }

    private fun getRepoName(uri: String): String {
        var lastIndex = uri.lastIndexOf('/')
        return uri.slice(++lastIndex..uri.lastIndex)
    }

    fun destroy() {
        if (repoDir.exists()) {
            Runtime.getRuntime().exec("rm -rf ${repoDir.absolutePath}")
            logger.info("仓库已删除")
        }
    }
}