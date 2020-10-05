package moe.nekonest.gdh.workingthreads

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
abstract class WorkingCoroutine {
    protected abstract var onStart: () -> Unit
    protected abstract var onComplete: (String) -> Unit
    protected abstract var onError: WorkingCoroutine.(Exception) -> Unit
    protected abstract var onProgress: (Int) -> Unit

    protected lateinit var job: Job

    fun onStart(onStart: () -> Unit): WorkingCoroutine {
        this.onStart = onStart
        return this
    }

    fun onComplete(onComplete: (String) -> Unit): WorkingCoroutine {
        this.onComplete = onComplete
        return this
    }

    fun onError(onError: WorkingCoroutine.(Exception) -> Unit): WorkingCoroutine {
        this.onError = onError
        return this
    }

    fun onProgress(onProgress: (Int) -> Unit): WorkingCoroutine {
        this.onProgress = onProgress
        return this
    }

    fun start(): Job {
        job = GlobalScope.launch {
            try {
                run()
            } catch (e: Exception) {
                onError(e)
            }
        }
        return job
    }

    fun cancel() {
        job.cancel()
    }

    protected abstract suspend fun run()
}