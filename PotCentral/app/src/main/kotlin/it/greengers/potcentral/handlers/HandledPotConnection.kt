package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.ConnectionManager
import it.greengers.potconnectors.connection.PotConnection

class HandledPotConnection(
    val connection : PotConnection,
    var messageHandler : ConnectionMessagesHandler = noOpMessagesHandlerFor(connection, false),
    var disconnectionHandler : DisconnectionHandler = noOpDisconnectionHandlerFor(connection, false)
)

fun PotConnection.handleConnection(
    messageHandler : ConnectionMessagesHandler = noOpMessagesHandlerFor(this, false),
    disconnectionHandler : DisconnectionHandler = noOpDisconnectionHandlerFor(this, false))
: HandledPotConnection {
    return HandledPotConnection(this, messageHandler, disconnectionHandler)
}

suspend fun HandledPotConnection.closeHandlers() {
    messageHandler.detachOnMessage()
    disconnectionHandler.detachOnDisconnection()
}
