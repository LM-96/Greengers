package it.greengers.potconnectors.concurrency

interface CoroutineCondition {

    /**
     * Causes the current coroutine to wait until it is signaled
     */
    suspend fun wait()

    /**
     * Wakes up one waiting coroutine
     */
    suspend fun signal()

    /**
     * Wakes up all waiting coroutines
     */
    suspend fun signalAll()

}