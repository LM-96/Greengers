package it.greengers.potserver.sensors

import kotlinx.coroutines.selects.select

object SensorFactory {

    fun <T> getInputSensor(id : String) : InputSensor<T>? {
        return when(id) {

            else -> null
        }
    }

    fun getMainId(type : SensorType) : String? {
        return when(type) {
            SensorType.TEMPERATURE -> "TEMP0"
            SensorType.BRIGHTNESS -> "BRIGHT0"
            SensorType.HUMIDITY -> "HUMIDITY0"
        }
    }

    fun getSensors() : List<Sensor> {
        return listOf()
    }

}