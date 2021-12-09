package it.greengers.potcentral.handlers

import it.greengers.potcentral.core.PotContext
import it.greengers.potconnectors.connection.PotConnection

class HandledPotConnection(
    val connection : PotConnection,
    private var messageHandler : ConnectionMessagesHandler = noOpMessagesHandlerFor(connection, false),
    private var disconnectionHandler : DisconnectionHandler = noOpDisconnectionHandlerFor(connection, false)
) {

    suspend fun switchMessagesHandler(messageHandler: ConnectionMessagesHandler, autoAttach : Boolean = true) {
        this.messageHandler.detachOnMessage()
        this.messageHandler = messageHandler
    }

}

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
