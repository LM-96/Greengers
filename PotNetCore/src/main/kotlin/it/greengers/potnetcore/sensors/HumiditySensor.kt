package it.greengers.potnetcore.sensors

class HumiditySensor(id : String)
    : PythonDoubleInputSensor("python/humidity.py", id, id, SensorType.HUMIDITY){
}