package it.greengers.potconnectors.utils

import it.greengers.potconnectors.concurrency.FlowedCondition
import it.greengers.potconnectors.concurrency.newCondition
import it.greengers.potconnectors.connection.PotConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.kotlin.loggerOf
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class Reconnector(
    private val connection : PotConnection,
    private val connectTime : Long = 5000,
    private val alwaysReconnect : Boolean = false,
    private val scope : CoroutineScope = SCOPE
) {

    companion object {
        @JvmStatic val LOGGER = loggerOf(this::class.java)
        @JvmStatic private val SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this.javaClass.simpleName))

        @JvmStatic fun tryReconnection(connection: PotConnection, connectTime: Long = 5000, alwaysReconnect: Boolean = false, scope: CoroutineScope = SCOPE) : Reconnector {
            return Reconnector(connection, connectTime, alwaysReconnect, scope)
        }

        @JvmStatic fun attachPersistentReconnector(connection: PotConnection, connectTime: Long = 5000) : Reconnector {
            return Reconnector(connection, connectTime, true)
        }
    }

    private val callback = this::tryReconnect
    private var currentJob : Job? = null
    private val mutex = Mutex()
    private val condition = mutex.newCondition()


    private suspend fun tryReconnect(reason : String) {
        withNullValue(currentJob) {
            mutex.lock()
            currentJob = scope.launch { //*********************************************************
                LOGGER.info("Started reconnect session for ${connection}")
                while (!connection.isConnected()) {
                    withError(connection.connect()) {
                        LOGGER.error("Error during reconnection for $connection: ${it.stackTraceToString()}")
                        delay(connectTime)
                    }
                }
                mutex.withLock {
                    currentJob = null
                    condition.signalAll()
                    if(!alwaysReconnect) connection.removeCallbackOnDisconnection(callback)
                }
            } //***********************************************************************************
            mutex.unlock()
        }
    }

    suspend fun stop() {
        mutex.lock()
        withNotNullValue(currentJob) {
            it.cancel()
            if(alwaysReconnect) connection.removeCallbackOnDisconnection(callback)
        }
        mutex.unlock()
    }

    suspend fun waitConnection() {
        mutex.withLock {
            while(currentJob != null)
                condition.wait()
        }
    }

}