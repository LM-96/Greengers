package it.greengers.potserver.sensors

import it.greengers.potnetcore.sensors.PythonDoubleInputSensor
import it.greengers.potnetcore.sensors.SensorType

class BrightnessSensor (id : String)
    : PythonDoubleInputSensor("python/Light.py", id, id, SensorType.TEMPERATURE) {
}