package it.greengers.potserver.sensors

object SensorFactory {

    private val sensors = mutableMapOf<String, Sensor>()

    init {
        sensors["TEMP0"] = TemperatureSensor("TEMP0")
        sensors["BRIGHT0"] = BrightnessSensor("BRIGHT0")
        sensors["HUMIDITY0"] = HumiditySensor("HUMIDITY0")
    }

    fun getSensor(id : String) : Sensor? {
        return sensors[id]
    }

    fun getSensorType(id : String) : SensorType? {
        return sensors[id]?.type
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