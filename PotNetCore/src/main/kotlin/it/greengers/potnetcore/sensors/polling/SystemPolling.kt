package it.greengers.potnetcore.sensors.polling

import it.greengers.potnetcore.controller.CurrentPlant
import it.greengers.potnetcore.controller.PotNetCoreCoreCoreController
import it.greengers.potnetcore.sensors.InputSensor
import it.greengers.potnetcore.sensors.SensorFactory
import it.greengers.potnetcore.sensors.SensorType

data class ManagedInputSensor<T>(
    val sensor : InputSensor<T>,
    val pollingJob : SensorPollingJob<T> = sensor.newPollingJob(),
    val listener : PollingListener<T> = pollingJob.buildDefaultListener()
) {
    suspend fun enableAndStart() : Error? {
        if(!sensor.isEnabled()) {
            val error = sensor.enable()
            if(error != null) return error
        }
        return pollingJob.start()

    }
}

fun <T> InputSensor<T>.autoManage() : ManagedInputSensor<T>{
    return ManagedInputSensor(this)
}

val TEMPERATURE_SENSOR : ManagedInputSensor<Double>? =
    (SensorFactory.getSensor(SensorType.TEMPERATURE) as InputSensor<Double>?)?.autoManage()

val HUMIDIY_SENSOR : ManagedInputSensor<Double>? =
    (SensorFactory.getSensor(SensorType.HUMIDITY) as InputSensor<Double>?)?.autoManage()

val BRIGHTNESS_SENSOR : ManagedInputSensor<Double>? =
    (SensorFactory.getSensor(SensorType.BRIGHTNESS) as InputSensor<Double>?)?.autoManage()

class DefaultSensorPollingListener<T>(
   job : SensorPollingJob<T>, autoAttach : Boolean = true) : PollingListener<T>(job, autoAttach){

    private val sensorType = job.sensor.type
    init {
        if(autoAttach)
            attach()
    }

    override suspend fun onPolling(value : T) {
        CurrentPlant.updateState(sensorType, value as Double)
        val range = CurrentPlant
            .safeProduceWithCurrentPlant { it.optimalPlantCondition.fromSensorType(sensorType) }

        if(range.isOutOfRange(value))
            PotNetCoreCoreCoreController.sendCriticalValue(sensorType, value)
    }

}

fun <T> SensorPollingJob<T>.buildDefaultListener(autoAttach : Boolean = true) : DefaultSensorPollingListener<T> {
    return DefaultSensorPollingListener(this, autoAttach)
}