package it.greengers.potserver.proxy

open class ServerMessage(
    val msgId : String,
    val senderName : String,
    val senderIp : String,
    val destination : String,
    val destinationIp : String,
    val type: ServerMessageType
) {

    override fun toString(): String {
        return "ServerMessage(msgId='$msgId', senderName='$senderName', senderIp='$senderIp', destination='$destination', destinationIp='$destinationIp', type=$type)"
    }

}