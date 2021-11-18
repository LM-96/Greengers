package it.greengers.potserver.proxy

class CommunicationMessage(
    msgId : String,
    senderName : String,
    senderIp : String,
    destination : String,
    destinationIp : String,
    val comunicationType : String,
    val comunication : String
) : ServerMessage(msgId, senderName, senderIp, destination, destinationIp, ServerMessageType.COMMUNICATION){

    override fun toString(): String {
        return "ComunicationMessage(comunicationType='$comunicationType', comunication='$comunication') ${super.toString()}"
    }
}