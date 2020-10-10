package moe.nekonest.gdh.workingthreads

import moe.lemonneko.nekogit.cmds.GitClone
import moe.nekonest.gdh.util.REPO_DIR
import moe.nekonest.gdh.util.ZipUtil
import moe.nekonest.gdh.util.getAvg
import org.slf4j.LoggerFactory
import java.io.File

@Suppress("ObjectLiteralToLambda")
class CloneThread(private val uri: String) : WorkingThread() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var output = ""
    private val repoDir = File(REPO_DIR, getRepoName(uri))
    override lateinit var onStart: () -> Unit
    override lateinit var onComplete: (String) -> Unit
    override lateinit var onError: WorkingThread.(Throwable) -> Unit
    override lateinit var onProgress: (Int) -> Unit

    private lateinit var onCompressing: () -> Unit

    fun onCompressing(onCompressing: () -> Unit): CloneThread {
        this.onCompressing = onCompressing
        return this
    }

    override fun run() {
        logger.info("start download code")
        onStart()

        var receive = 0
        var indexed = 0
        var checkout = 0

        val fetchCallback = object : GitClone.FetchCallback {
            override fun progress(p0: Int, p1: Int) {
                receive = p0
                indexed = p1
                onProgress(getAvg(receive, indexed, checkout))
            }
        }

        val checkoutCallback = object : GitClone.CheckoutCallback {
            override fun progress(p0: Int) {
                checkout = p0
                onProgress(getAvg(receive, indexed, checkout))
            }
        }

        val errorCallback = object : GitClone.ErrorCallback {
            override fun handleError(p0: Throwable?) {
                p0 ?: return
                onError(p0)
            }
        }

        GitClone.doClone(
            uri,
            repoDir.absolutePath,
            fetchCallback,
            checkoutCallback,
            errorCallback
        )

        output = ZipUtil.compress(repoDir.absolutePath)

        onComplete(output)
    }

    private fun getRepoName(uri: String): String {
        var lastIndex = uri.lastIndexOf('/')
        return uri.slice(++lastIndex..uri.lastIndex)
    }
}