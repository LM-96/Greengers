package it.greengers.potserver.sensors

import java.io.IOException
import kotlin.jvm.Throws

abstract class OutputSensor<T>(id : String, name : String, enabled : Boolean = true) : Sensor(id, name, enabled) {

    @Throws(IOException::class)
    protected abstract fun handleWriteRequest(data : T)

    @Throws(IllegalStateException::class, IOException::class)
    fun write(data : T){
        if(enabled) {
            return handleWriteRequest(data)
        }
        throw IllegalStateException("Sensor [${toString()}] is disabled. Cannot write.")
    }
}