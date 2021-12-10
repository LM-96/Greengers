package it.greengers.potserver.sensors

class BrightnessSensor (id : String)
    : PythonDoubleInputSensor("python/brightness.py", id, id, SensorType.TEMPERATURE) {
}