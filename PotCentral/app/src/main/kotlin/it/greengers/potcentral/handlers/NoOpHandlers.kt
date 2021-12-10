package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.PotMessage

class NoOpMessagesHandler(connection: PotConnection, autoAttach : Boolean = true)
    : AbstractConnectionMessagesHandler(connection, autoAttach) {
    override suspend fun onMessage(msg: PotMessage) {}
}

class NoOpDisconnectionHandler(connection: PotConnection, autoAttach : Boolean = true)
    : AbstractDisconnectionHandler(connection, autoAttach){
    override suspend fun onDisconnection(reason: String) {}
}

fun noOpMessagesHandlerFor(connection: PotConnection, autoAttach: Boolean = true) : AbstractConnectionMessagesHandler {
    return NoOpMessagesHandler(connection, autoAttach)
}

fun noOpDisconnectionHandlerFor(connection: PotConnection, autoAttach: Boolean) : AbstractDisconnectionHandler {
    return NoOpDisconnectionHandler(connection, autoAttach)
}