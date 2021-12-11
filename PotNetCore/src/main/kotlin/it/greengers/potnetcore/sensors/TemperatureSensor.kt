package it.greengers.potnetcore.sensors

class TemperatureSensor(id : String)
    : PythonDoubleInputSensor("python/Temperature.py", id, id, SensorType.TEMPERATURE) {
}