package it.greengers.potserver.proxy

class ValueOutOfRangeMessage(
    msgId : String,
    senderName : String,
    senderIp : String,
    destination : String,
    destinationIp : String,
    val valueType : String,
    val value : String,
) : ServerMessage(msgId, senderName, senderIp, destination, destinationIp, ServerMessageType.VALUE_OUT_OF_RANGE){
}