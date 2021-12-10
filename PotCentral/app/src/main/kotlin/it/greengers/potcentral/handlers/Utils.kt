package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.*

fun PotMessage.isForMe() : Boolean {
    return destinationName == LocalPotDNS.getApplicationName()
}

fun PotMessage.isNotForMe() : Boolean {
    return destinationName != LocalPotDNS.getApplicationName()
}

fun PotMessage.ifIsForMe(action : (PotMessage) -> Unit) : PotMessage {
    if(destinationName == LocalPotDNS.getApplicationName())
        action.invoke(this)

    return this
}

fun PotMessage.ifNotForMe(action : (PotMessage) -> Unit) : PotMessage {
    if(destinationName != LocalPotDNS.getApplicationName())
        action.invoke(this)

    return this
}

fun CommunicationMessage.buildRedirection(destinationName : String, senderName : String = LocalPotDNS.getApplicationName()) : CommunicationMessage {
    return buildCommunicationMessage(communicationType, communication, destinationName, senderName)
}

fun StateRequestMessage.buildRedirection(destinationName : String, senderName : String = LocalPotDNS.getApplicationName()) : StateRequestMessage {
    return buildStateRequestMessage(destinationName, senderName)
}