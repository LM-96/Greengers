package it.greengers.potnetcore.sensors

class HumiditySensor(id : String)
    : PythonDoubleInputSensor("python/Humidity.py", id, id, SensorType.HUMIDITY){
}