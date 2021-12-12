package it.greengers.potnetcore.core

import it.greengers.potconnectors.utils.FunResult
import it.greengers.potconnectors.utils.toFunResult
import it.greengers.potnetcore.sensors.Sensor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.kotlin.logger
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

object MasterContext {

    private val LOGGER = logger("MasterContext")
    val SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this::class.java.name))
    private val MASTERS = mutableMapOf<String, SensorMaster>()
    private val MUTEX = Mutex()
    private val flow = MutableSharedFlow<MasterMessage>(replay = 0)

    suspend fun newMasterFor(sensor : Sensor) : SensorMaster {
        val master : SensorMaster = OwnerSensorMaster(sensor, SCOPE, flow)
        MUTEX.withLock {
            MASTERS[sensor.id] = master
        }

        return master
    }

    suspend fun send(msg : MasterMessage) : Error? {
        MUTEX.withLock {
            val master = MASTERS[msg.destination]
            if(master == null)
                return Error("Unable to find a master with id [${msg.destination}]")

            master.getOutput().send(msg)
            return null
        }
    }

    suspend fun request(msg : MasterMessage) : FunResult<MasterMessage> {
        val master : SensorMaster?
        MUTEX.withLock {
            master = MASTERS[msg.destination]
        }
        if(master == null)
            return FunResult.fromErrorString("Unable to find a master with id [${msg.destination}]")
        return master.toFunResult()
    }

}