package moe.nekonest.gdh.workingthreads

abstract class WorkingThread {
    protected abstract var onStart: () -> Unit
    protected abstract var onComplete: (String) -> Unit
    protected abstract var onError: WorkingThread.(Throwable) -> Unit
    protected abstract var onProgress: (Int) -> Unit

    fun onStart(onStart: () -> Unit): WorkingThread {
        this.onStart = onStart
        return this
    }

    fun onComplete(onComplete: (String) -> Unit): WorkingThread {
        this.onComplete = onComplete
        return this
    }

    fun onError(onError: WorkingThread.(Throwable) -> Unit): WorkingThread {
        this.onError = onError
        return this
    }

    fun onProgress(onProgress: (Int) -> Unit): WorkingThread {
        this.onProgress = onProgress
        return this
    }

    fun start(): Thread {
        val t = Thread {
            try {
                run()
            } catch (e: Throwable) {
                onError(e)
            }
        }

        t.start()
        return t
    }

    protected abstract fun run()
}