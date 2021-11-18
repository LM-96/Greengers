package it.greengers.potconnectors.messages

abstract class PotMessage (
    val destinationName : String,
    val senderName : String,
    val type : PotMessageType
        ){
    override fun toString(): String {
        return "PotMessage(destinationName='$destinationName', senderName='$senderName', type=$type)"
    }
}