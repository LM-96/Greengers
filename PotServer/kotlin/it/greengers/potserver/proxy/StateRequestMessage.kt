package it.greengers.potserver.proxy

class StateRequestMessage(
    msgId : String,
    senderName : String,
    senderIp : String,
    destination : String,
    destinationIp : String,
) : ServerMessage(msgId, senderName, senderIp, destination, destinationIp, ServerMessageType.STATE_REQUEST) {
}