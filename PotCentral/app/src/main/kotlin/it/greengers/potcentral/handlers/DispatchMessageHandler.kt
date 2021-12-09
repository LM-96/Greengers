package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.*

abstract class DispatchMessageHandler(connection : PotConnection, autoAttach : Boolean = true)
    : AbstractConnectionMessagesHandler(connection, autoAttach)
{
    override suspend fun onMessage(msg : PotMessage) {
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

    protected abstract suspend fun handleActorMessage(msg : ActorMessage)
    protected abstract suspend fun handleCommunicationMessage(msg : CommunicationMessage)
    protected abstract suspend fun handleErrorMessage(msg : ErrorMessage)
    protected abstract suspend fun handleStateRequestMessage(msg : StateRequestMessage)
    protected abstract suspend fun handleStateReplyMessage(msg : StateReplyMessage)
    protected abstract suspend fun handleUnsupportedOperationMessage(msg : UnsupportedOperationMessage)
    protected abstract suspend fun handleValueOutOfRangeMessage(msg : ValueOutOfRangeMessage)

}