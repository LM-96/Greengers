package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.PotConnection
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KSuspendFunction1

abstract class AbstractDisconnectionHandler(
    private val connection : PotConnection,
    autoAttach : Boolean = true
) : DisconnectionHandler {

    private val ON_DISCONNECTION = getCallback()

    init {
        if(autoAttach)
            runBlocking { attachOnDisconnection() }
    }

    private fun getCallback() : KSuspendFunction1<String, Unit> {
        return this::onDisconnection
    }

    override suspend fun attachOnDisconnection() {
        connection.addCallbackOnDisconnection(ON_DISCONNECTION)
    }

    override suspend fun detachOnDisconnection() {
        connection.removeCallbackOnDisconnection(ON_DISCONNECTION)
    }

}