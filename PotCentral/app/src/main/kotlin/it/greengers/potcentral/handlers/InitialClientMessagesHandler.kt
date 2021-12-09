package it.greengers.potcentral.handlers

import it.greengers.potcentral.core.PotContextContainer
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.BuiltInCommunicationType
import it.greengers.potconnectors.messages.CommunicationMessage
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.messages.PotMessageType

class InitialClientMessagesHandler(potConnection: PotConnection, autoAttach : Boolean = true)
: AbstractConnectionMessagesHandler(potConnection, autoAttach) {

    override suspend fun onMessage(msg: PotMessage) {
        if(msg.type == PotMessageType.COMMUNICATION) {
            msg as CommunicationMessage
            val commType = msg.isBuiltInCommunicationType()

            if(commType.isPresent){
                if(commType.get() == BuiltInCommunicationType.MYPOTIS) {
                    handleAssociation(msg.communication)
                    return

                } else if(commType.get() == BuiltInCommunicationType.POTREGISTERED){
                    handleRegistration(msg.communication)
                    return

                } else if (commType.get() == BuiltInCommunicationType.POTDEREGISTERED) {
                    handleDeRegistration(msg.communication)
                    return
                }
            }
        }

        potConnection.sendAsyncError("Please associate connection with pot or send a registration/deregistration message")
        println("Received unsupported message [$msg] from connection [$potConnection]")
    }

    private suspend fun handleAssociation(potId : String) {
        val ctx = PotContextContainer.getContext(potId)
        if(ctx != null) {
            detachOnMessage()
            val clientHandler = ClientMessagesHandler(potConnection)
            val handledConnection = potConnection.handleConnection(clientHandler)
            if(potConnection.destinationName == potId)
                ctx.potConnection = handledConnection
            else
                ctx.clientConnections.add(handledConnection)
        } else {
            potConnection.sendAsyncError("Invalid PotId: not registered")
        }
    }

    private suspend fun handleRegistration(potId: String) {
        val ctx = PotContextContainer.newContext(potId)
        val clientHandler = ClientMessagesHandler(potConnection)
        val handledConnection = potConnection.handleConnection(clientHandler)
        if(potConnection.destinationName == potId)
            ctx.potConnection = handledConnection
        else
            ctx.clientConnections.add(handledConnection)

        println("Registered new PotContext for Pot[$potId]")
    }

    private suspend fun handleDeRegistration(potId: String) {
        val ctx = PotContextContainer.closeContext(potId)
        println("De-Registered PotContext for Pot[$potId]")
    }
}