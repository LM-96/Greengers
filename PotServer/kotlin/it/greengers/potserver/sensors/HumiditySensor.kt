package it.greengers.potserver.sensors

class HumiditySensor(id : String)
    : PythonDoubleInputSensor("python/humidity.py", id, id, SensorType.HUMIDITY){
}