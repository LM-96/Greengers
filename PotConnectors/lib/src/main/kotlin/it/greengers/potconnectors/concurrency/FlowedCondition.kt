package it.greengers.potconnectors.concurrency

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import java.util.*

class FlowedCondition constructor(eMutex : Mutex): AbstractCoroutineCondition(eMutex) {

    private val flow = MutableSharedFlow<Optional<Job>>(0, 0)
    private val sharedFlow = flow.asSharedFlow()

    override suspend fun suspend() {
        try {
            sharedFlow.collect {
                if(it.isPresent) {
                    if (it.get() == currentCoroutineContext().job)
                        throw TerminateCollectException()
                } else {
                    throw TerminateCollectException()
                }
            }
        } catch (e : TerminateCollectException) {

        }
    }

    override suspend fun doSignal(job: Job) {
        flow.emit(Optional.of(job))
    }

    override suspend fun doSignalAll() {
        flow.emit(Optional.empty())
    }
}

private class TerminateCollectException : Exception() {}

fun Mutex.newCondition() : CoroutineCondition {
    return FlowedCondition(this)
}