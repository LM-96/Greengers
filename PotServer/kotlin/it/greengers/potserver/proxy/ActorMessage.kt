package it.greengers.potserver.proxy

class ActorMessage(
    msgId : String,
    senderName : String,
    senderIp : String,
    destination : String,
    destinationIp : String,
    val msg : String
) : ServerMessage(msgId, senderName, senderIp, destination, destinationIp, ServerMessageType.ACTOR) {
}