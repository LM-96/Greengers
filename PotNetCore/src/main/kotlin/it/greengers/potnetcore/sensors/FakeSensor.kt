package it.greengers.potnetcore.sensors

class FakeSensor(
    id : String,
    name : String,
    type : SensorType
) : InputSensor<Double>(id, name, type){

    override fun handleReadRequest(): Double {
        return 1.0
    }

    override fun handleEnableRequest(): Error? {
        return null
    }

    override fun handleDisableRequest(): Error? {
        return null
    }

    override fun close() {
    }
}