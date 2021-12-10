package it.greengers.potconnectors.concurrency

import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock


abstract class AbstractCoroutineCondition protected constructor(private val eMutex : Mutex) : CoroutineCondition {

    private val iMutex = Mutex()
    private val deque = ArrayDeque<Job>()

    abstract suspend fun suspend()
    abstract suspend fun doSignal(job : Job)
    abstract suspend fun doSignalAll()

    override suspend fun wait() {
        iMutex.withLock {
            deque.add(currentCoroutineContext().job)
        }
        try {
            eMutex.unlock()
        } catch (e : Exception) {
            iMutex.withLock { deque.remove(currentCoroutineContext().job) }
            throw e
        }
        suspend()
        eMutex.lock()
    }

    override suspend fun signal() {
        val job : Job?
        iMutex.withLock{
            job = deque.removeFirstOrNull()
        }

        if(job != null)
            doSignal(job)
    }

    override suspend fun signalAll() {
        val job : List<Job>
        iMutex.withLock{
            job = deque.toList()
            deque.clear()
            doSignalAll()
        }
    }

}