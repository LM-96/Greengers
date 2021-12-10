package it.greengers.potcentral.handlers

import it.greengers.potcentral.core.PotContextContainer
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.*

class ClientMessagesHandler(potConnection: PotConnection, autoAttach : Boolean = true)
    : DispatchMessageHandler(potConnection, autoAttach) {


    override suspend fun handleActorMessage(msg: ActorMessage) {
        potConnection.sendUnsupportedOperationMessage("QAK not available on the main server", msg)
    }

    override suspend fun handleCommunicationMessage(msg: CommunicationMessage) {
        val commType = msg.isBuiltInCommunicationType()
        if(commType.isPresent) {
            when(commType.get()) {

                BuiltInCommunicationType.PLANTCHANGE -> {
                    proxy(msg.buildRedirection(msg.communication, msg.senderName))
                }

                BuiltInCommunicationType.COMPLETE_STATE -> {
                    val ctx = PotContextContainer.getContext(msg.senderName)
                    if(ctx != null) {
                        if(ctx.potConnection?.connection?.destinationName == msg.destinationName) {
                            ctx.clientConnections.forEach { it.connection.sendAsyncMessage(msg) }
                        }
                    }
                }

                BuiltInCommunicationType.REQUEST_COMPLETE_STATE -> {
                    proxy(msg.buildRedirection(msg.communication, msg.senderName))
                }
            }
        }
    }

    override suspend fun handleErrorMessage(msg: ErrorMessage) {
        println("Received error from ${msg.senderName}: ${msg.errorDescription}")
    }

    override suspend fun handleStateRequestMessage(msg: StateRequestMessage) {
        potConnection.sendUnsupportedOperationMessage("Please send the message directly to the pot", msg)
    }

    override suspend fun handleStateReplyMessage(msg: StateReplyMessage) {
        potConnection.sendUnsupportedOperationMessage("Please send the message directly to the pot", msg)
    }

    override suspend fun handleUnsupportedOperationMessage(msg: UnsupportedOperationMessage) {
        println("Received UnsupportedOperationMessage from ${msg.senderName}")
    }

    override suspend fun handleValueOutOfRangeMessage(msg: ValueOutOfRangeMessage) {
        val ctx = PotContextContainer.getContext(msg.senderName)
        if(ctx != null) {
            if(ctx.potConnection?.connection?.destinationName == msg.destinationName) {
                ctx.clientConnections.forEach { it.connection.sendAsyncMessage(msg) }
            }
        }
    }


}