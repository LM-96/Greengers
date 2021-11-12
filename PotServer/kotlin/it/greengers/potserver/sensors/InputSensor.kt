package it.greengers.potserver.sensors

interface InputSensor<T> {

    val sensorName : String
    val id : String

    fun start()
    fun stop()

    fun read() : T
}