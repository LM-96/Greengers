package it.greengers.potcentral.core

import it.greengers.potcentral.handlers.closeHandlers
import it.greengers.potconnectors.messages.BuiltInCommunicationType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object PotContextContainer {

    private val CONTEXTS = mutableMapOf<String, PotContext>()
    private val MUTEX = Mutex()

    suspend fun newContext(potId : String) : PotContext {
        val ctx = PotContext(potId)
        MUTEX.withLock {
            CONTEXTS[potId] = ctx
        }

        return ctx
    }

    suspend fun getContext(potId: String) : PotContext? {
        MUTEX.withLock {
            return CONTEXTS[potId]
        }
    }

    suspend fun closeContext(potId: String) {
        val ctx : PotContext?
        MUTEX.withLock {
            ctx = CONTEXTS.remove(potId)
        }

        ctx?.clientConnections?.forEach {
            if(it.connection.isConnected()) {
                it.connection.sendAsyncCommunication(BuiltInCommunicationType.POTDEREGISTERED, "Pot context is closed")
                it.connection.disconnect("Pot context has been closed")
            }
            it.closeHandlers()
        }
    }

}