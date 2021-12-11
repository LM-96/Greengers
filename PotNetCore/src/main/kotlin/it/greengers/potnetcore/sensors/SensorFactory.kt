package it.greengers.potnetcore.sensors

import it.greengers.potnetcore.controller.Settings
import it.greengers.potserver.sensors.BrightnessSensor

object SensorFactory {

    private val sensors = mutableMapOf<String, Sensor>()

    init {
        sensors["TEMP0"] = TemperatureSensor("TEMP0")
        sensors["BRIGHT0"] = BrightnessSensor("BRIGHT0")
        sensors["HUMIDITY0"] = HumiditySensor("HUMIDITY0")
        /*sensors["TEMP0"] = FakeSensor("TEMP0", "TEMP0", SensorType.TEMPERATURE)
        sensors["BRIGHT0"] = FakeSensor("BRIGHT0", "BRIGHT0", SensorType.BRIGHTNESS)
        sensors["HUMIDITY0"] = FakeSensor("HUMIDITY0", "HUMIDITY0", SensorType.HUMIDITY)*/
    }

    fun getSensor(id : String) : Sensor? {
        return sensors[id]
    }

    fun getSensorType(id : String) : SensorType? {
        return sensors[id]?.type
    }

    fun getSensor(type : SensorType) : Sensor? {
        return sensors[getMainId(type)]
    }

    fun getMainId(type : SensorType) : String {
        return when(type) {
            SensorType.TEMPERATURE -> "TEMP0"
            SensorType.BRIGHTNESS -> "BRIGHT0"
            SensorType.HUMIDITY -> "HUMIDITY0"
        }
    }

    fun getSensors() : List<Sensor> {
        return sensors.values.toList()
    }

    fun getSensorIds() : List<String> {
        return sensors.keys.toList()
    }

}