package it.greengers.potserver.sensors

import java.io.BufferedReader
import java.io.InputStreamReader

open class PythonDoubleInputSensor(
    private val script : String,
    id : String,
    name : String,
    type : SensorType) : InputSensor<Double>(id, name, type) {

    override fun handleReadRequest(): Double {
        val proc = Runtime.getRuntime().exec(script)
        val reader = BufferedReader(InputStreamReader(proc.inputStream))
        val value = reader.readLine()
        val res : Double
        try {
            res = value.toDouble()
        } catch (e : Exception) {
            println("Sensor[$type] | Unable to read value\n${e.stackTraceToString()}")
            throw e
        }

        return res
    }

    override fun handleEnableRequest(): Error? {
        return Error("Sensor can only be enabled")
    }

    override fun handleDisableRequest(): Error? {
        return Error("Sensor can only be enabled")
    }

    override fun close() {

    }
}