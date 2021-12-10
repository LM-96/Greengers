package it.greengers.potserver.sensors

class TemperatureSensor(id : String)
    : PythonDoubleInputSensor("python/temperature.py", id, id, SensorType.TEMPERATURE) {
}