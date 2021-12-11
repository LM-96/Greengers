package it.greengers.potnetcore.sensors.polling

import it.greengers.potconnectors.utils.FunResult
import it.greengers.potconnectors.utils.withExceptionToFunResult
import it.greengers.potnetcore.sensors.InputSensor
import kotlinx.coroutines.*

class SensorPollingJob<T>(val sensor : InputSensor<T>, scope : CoroutineScope) : AbstractPollingJob<T>(sensor.id, scope) {

    override fun read(): FunResult<T> {
        return withExceptionToFunResult { sensor.read() }
    }

    override suspend fun doStart(): Error? {
        return null
    }

    override suspend fun doPause(): Error? {
        return null
    }

    override suspend fun doStop(): Error? {
        return null
    }
}