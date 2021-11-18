package it.greengers.potserver.proxy

class StateReplyMessage(
    msgId : String,
    senderName : String,
    senderIp : String,
    destination : String,
    destinationIp : String,
    val temperature : Double,
    val humidity : Double,
    val brightness : Double,
    val battery : Double
) : ServerMessage(msgId, senderName, senderIp, destination, destinationIp, ServerMessageType.STATE_REPLY){
}