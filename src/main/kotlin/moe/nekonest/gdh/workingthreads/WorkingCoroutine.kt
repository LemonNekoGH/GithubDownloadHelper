package moe.nekonest.gdh.workingthreads

@Suppress("UNCHECKED_CAST")
abstract class WorkingCoroutine {
    protected abstract var onStart: () -> Unit
    protected abstract var onComplete: (String) -> Unit
    protected abstract var onError: (Exception) -> Unit

    fun onStart(onStart: () -> Unit): WorkingCoroutine {
        this.onStart = onStart
        return this
    }

    fun onComplete(onComplete: (String) -> Unit): WorkingCoroutine {
        this.onComplete = onComplete
        return this
    }

    fun onError(onError: (Exception) -> Unit): WorkingCoroutine {
        this.onError = onError
        return this
    }

    suspend fun start() {
        try {
            run()
        } catch (e: Exception) {
            onError(e)
        }
    }

    protected abstract suspend fun run()
}