package it.greengers.potserver.sensors

import java.io.IOException
import kotlin.jvm.Throws

abstract class InputSensor<T>(id : String, name : String, enabled : Boolean = true) : Sensor(id, name, enabled) {

    @Throws(IOException::class)
    protected abstract fun handleReadRequest() : T

    @Throws(IllegalStateException::class, IOException::class)
    fun read() : T {
        if(enabled) {
            return handleReadRequest()
        }
        throw IllegalStateException("Sensor [${toString()}] is disabled. Cannot read.")
    }
}