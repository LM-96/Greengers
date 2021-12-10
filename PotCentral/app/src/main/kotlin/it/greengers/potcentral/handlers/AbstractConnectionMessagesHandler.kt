package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.*
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KSuspendFunction1

abstract class AbstractConnectionMessagesHandler(
    protected val potConnection: PotConnection,
    autoAttach : Boolean = true
) : ConnectionMessagesHandler {

    private val ON_MESSAGE = getCallback()

    init {
        if(autoAttach)
            runBlocking { attachOnMessage() }
    }

    private fun getCallback() : KSuspendFunction1<PotMessage, Unit> {
        return this::onMessage
    }

    override suspend fun attachOnMessage() {
        potConnection.addCallbackOnMessage(ON_MESSAGE)
    }

    override suspend fun detachOnMessage() {
        potConnection.removeCallbackOnMessage(ON_MESSAGE)
    }
}