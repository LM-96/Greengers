package it.greengers.potnetcore.sensors.polling

import it.greengers.potnetcore.sensors.InputSensor
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.EmptyCoroutineContext

interface PollingJob<T> {

    companion object {
        val POLLING_SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this::class.java.name))
    }

    suspend fun start() : Error?
    suspend fun pause() : Error?
    suspend fun stop() : Error?
    suspend fun waitForNext() : T
    fun getJob() : Job
    fun getCurrentState() : PollingJobState
    fun attachOnPolling(action : suspend (T) -> Unit)
    fun detachOnPolling(action : suspend (T) -> Unit)

}

enum class PollingJobState {
    READY, WORKING, PAUSED, STOPPED
}

fun <T> InputSensor<T>.newPollingJob() : SensorPollingJob<T> {
    return SensorPollingJob(this, PollingJob.POLLING_SCOPE)
}