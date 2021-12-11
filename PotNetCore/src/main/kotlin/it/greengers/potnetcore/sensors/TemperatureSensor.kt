package it.greengers.potnetcore.sensors

class TemperatureSensor(id : String)
    : PythonDoubleInputSensor("python/temperature.py", id, id, SensorType.TEMPERATURE) {
}