package it.greengers.potnetcore.core

import it.greengers.potnetcore.sensors.Sensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharedFlow

class OwnerSensorMaster(sensor: Sensor, scope : CoroutineScope,
                        ctxFlow : SharedFlow<MasterMessage>,
                        input : Channel<MasterMessage> = Channel(),
                        output : Channel<MasterMessage> = Channel()
) : SensorMaster(sensor, scope, ctxFlow, input, output) {

    override suspend fun onRequest(msg: MasterMessage) {
        TODO("Not yet implemented")
    }

    override suspend fun onReply(msg: MasterMessage) {
        TODO("Not yet implemented")
    }

    override fun onDispatch(msg: MasterMessage) {
        TODO("Not yet implemented")
    }

    override fun onEvent(msg: MasterMessage) {
        TODO("Not yet implemented")
    }
}