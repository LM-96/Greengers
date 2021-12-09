package it.greengers.potconnectors.messages

import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.utils.FunResult
import it.greengers.potconnectors.utils.buildValidApplMessage
import it.unibo.kactor.ApplMessage

fun buildActorMessage(msg : String, destination : String, sender : String = LocalPotDNS.getApplicationName()) : FunResult<ActorMessage> {
    val applMsg = buildValidApplMessage(msg)
    if(applMsg.thereIsError())
        return applMsg.castWithError()

    return FunResult(buildActorMessage(applMsg.res!!, destination))
}

fun buildActorMessage(msg : ApplMessage, destination: String, sender: String = LocalPotDNS.getApplicationName()) : ActorMessage {
    return ActorMessage(destination, sender, msg)
}

fun buildCommunicationMessage(communicationType : String, communication : String, destination: String, sender: String = LocalPotDNS.getApplicationName()) : CommunicationMessage {
    return CommunicationMessage(destination, sender, communicationType, communication)
}

fun buildCommunicationMessage(communicationType : BuiltInCommunicationType, communication : String, destination: String, sender: String = LocalPotDNS.getApplicationName()) : CommunicationMessage {
    return CommunicationMessage(destination, sender, communicationType.toString(), communication)
}

fun buildErrorMessage(errorDescription : String, destination: String, sender: String = LocalPotDNS.getApplicationName()) : ErrorMessage {
    return ErrorMessage(destination, sender, errorDescription)
}

fun buildErrorMessage(throwable : Throwable, destination: String, sender: String = LocalPotDNS.getApplicationName()) : ErrorMessage {
    return buildErrorMessage("KotlinError[$throwable]", destination, sender)
}

fun buildStateReplyMessage(temperature : Double, humidity : Double, brightness : Double,
                           battery : Double, destination : String,
                           sender : String = LocalPotDNS.getApplicationName()) : StateReplyMessage {
    return StateReplyMessage(destination, sender, temperature, humidity, brightness, battery)
}

fun buildStateRequestMessage(destination: String, sender: String = LocalPotDNS.getApplicationName()) : StateRequestMessage {
    return StateRequestMessage(destination, sender)
}

fun buildUnsupportedOperationMessage(explanation : String? = null, referredMessage : PotMessage? = null,
                                     destinationName: String, senderName: String = LocalPotDNS.getApplicationName()) : UnsupportedOperationMessage {
    return UnsupportedOperationMessage(destinationName, senderName, explanation, referredMessage)
}

fun buildValueOutOfRangeMessage(valueType : String, value : Any, destination: String, sender: String = LocalPotDNS.getApplicationName()) : ValueOutOfRangeMessage {
    return ValueOutOfRangeMessage(destination, sender, valueType, value.toString())
}