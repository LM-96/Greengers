package it.greengers.potnetcore.sensors.polling

import it.greengers.potnetcore.controller.CurrentPlant
import it.greengers.potnetcore.sensors.InputSensor
import it.greengers.potnetcore.sensors.SensorFactory
import it.greengers.potnetcore.sensors.SensorType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

val TEMPERATURE_JOB : PollingJob<Double>? =
    (SensorFactory.getSensor(SensorType.TEMPERATURE) as InputSensor<Double>?)?.newPollingJob()

val HUMIDIY_JOB : PollingJob<Double>? =
    (SensorFactory.getSensor(SensorType.HUMIDITY) as InputSensor<Double>?)?.newPollingJob()

val BRIGHTNESS_JOB : PollingJob<Double>? =
    (SensorFactory.getSensor(SensorType.BRIGHTNESS) as InputSensor<Double>?)?.newPollingJob()

class DefaultSensorPollingListener<T>(
    private val job : SensorPollingJob<T>, autoAttach : Boolean = true) {

    private val sensorType = job.sensor.type
    private val callback = this::onPolling
    init {
        if(autoAttach)
            attach()
    }

    fun attach() {
        job.attachOnPolling(callback)
    }

    fun detach() {
        job.detachOnPolling(callback)
    }

    private suspend fun onPolling(value : T) {
        CurrentPlant.updateState(sensorType, value as Double)

    }

}