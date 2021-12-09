package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.PotMessage

class ClientMessagesHandler(potConnection: PotConnection, autoAttach : Boolean = true)
    : AbstractConnectionMessagesHandler(potConnection, autoAttach) {

    override suspend fun onMessage(msg: PotMessage) {
        TODO("Not yet implemented")
    }
}