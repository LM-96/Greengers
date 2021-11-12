package it.greengers.potserver.sensors

import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

abstract class Sensor(
    val sensorName : String,
    val id : String,
    protected var enabled : Boolean = true
) : Closeable, AutoCloseable {

    protected abstract fun handleEnableRequest() : Error?
    protected abstract fun handleDisableRequest() : Error?

    fun enable() : Error? {
        if(!enabled) {
            val err = handleEnableRequest()
            if(err == null) enabled = true
            return err
        }
        return Error("Sensor already enabled")
    }

    fun disable() : Error? {
        if(enabled) {
            val err = handleDisableRequest()
            if(err == null) enabled = false
            return err
        }
        return Error("Sensor already disabled")
    }

}