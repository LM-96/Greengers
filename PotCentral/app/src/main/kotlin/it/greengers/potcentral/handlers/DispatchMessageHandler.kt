package it.greengers.potcentral.handlers

import it.greengers.potcentral.core.PotContextContainer
import it.greengers.potconnectors.connection.ConnectionManager
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.*

abstract class DispatchMessageHandler(connection : PotConnection, autoAttach : Boolean = true)
    : AbstractConnectionMessagesHandler(connection, autoAttach)
{
    override suspend fun onMessage(msg : PotMessage) {
        if(!proxy(msg)) {
            when(msg.type) {
                PotMessageType.ACTOR -> handleActorMessage(msg as ActorMessage)
                PotMessageType.COMMUNICATION -> handleCommunicationMessage(msg as CommunicationMessage)
                PotMessageType.ERROR -> handleErrorMessage(msg as ErrorMessage)
                PotMessageType.STATE_REQUEST -> handleStateRequestMessage(msg as StateRequestMessage)
                PotMessageType.STATE_REPLY -> handleStateReplyMessage(msg as StateReplyMessage)
                PotMessageType.UNSUPPORTED_OPERATION -> handleUnsupportedOperationMessage(msg as UnsupportedOperationMessage)
                PotMessageType.VALUE_OUT_OF_RANGE -> handleValueOutOfRangeMessage(msg as ValueOutOfRangeMessage)
            }
        }
    }

    /**
     * Check if the destination of the given message is not this application then redirect
     * the message to the correct destination if there are some connection opened to it.
     *
     * @param msg the message
     * @return *true* if the desctination is not the currrent application, false otherwise
     */
    protected suspend fun proxy(msg : PotMessage) : Boolean {
        if(msg.destinationName != LocalPotDNS.getApplicationName()) {
            findConnection(msg)?.sendAsyncMessage(msg) ?: potConnection.sendUnsupportedOperationMessage("Unable to connect with remote host [${msg.destinationName}]", msg)
        return true
        }

        return false
    }

    /**
     * Find a connection for the destination of a message searching into
     * *PotContextContainer* and *ConnectionManager*
     *
     * @param msg the message to be sent
     * @return a *PotConnection* if found, otherwise null
     */
    protected suspend fun findConnection(msg : PotMessage) : PotConnection? {
        val ctx = PotContextContainer.getContext(msg.destinationName)
        if(ctx != null) {
            return ctx.potConnection?.connection
        } else {
            val optConn = ConnectionManager.getConnection(msg.destinationName)
            if(optConn.isPresent)
                return optConn.get()
        }

        return null
    }

    protected abstract suspend fun handleActorMessage(msg : ActorMessage)
    protected abstract suspend fun handleCommunicationMessage(msg : CommunicationMessage)
    protected abstract suspend fun handleErrorMessage(msg : ErrorMessage)
    protected abstract suspend fun handleStateRequestMessage(msg : StateRequestMessage)
    protected abstract suspend fun handleStateReplyMessage(msg : StateReplyMessage)
    protected abstract suspend fun handleUnsupportedOperationMessage(msg : UnsupportedOperationMessage)
    protected abstract suspend fun handleValueOutOfRangeMessage(msg : ValueOutOfRangeMessage)

}