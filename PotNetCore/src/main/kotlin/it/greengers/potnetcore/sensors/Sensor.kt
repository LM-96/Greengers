package it.greengers.potnetcore.sensors

import java.io.Closeable

abstract class Sensor(
    val sensorName : String,
    val id : String,
    val type : SensorType,
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

    fun isEnabled() : Boolean {
        return enabled
    }

    override fun toString(): String {
        return "Sensor(sensorName='$sensorName', id='$id', enabled=$enabled)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sensor) return false

        if (sensorName != other.sensorName) return false
        if (id != other.id) return false
        if (enabled != other.enabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sensorName.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

}