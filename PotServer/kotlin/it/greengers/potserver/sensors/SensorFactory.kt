package it.greengers.potserver.sensors

import kotlinx.coroutines.selects.select

object SensorFactory {

    fun <T> getInputSensor(id : String) : InputSensor<T>? {
        return when {

            else -> null
        }
    }

}