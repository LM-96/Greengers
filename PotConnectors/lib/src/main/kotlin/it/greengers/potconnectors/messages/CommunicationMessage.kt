package it.greengers.potconnectors.messages

class CommunicationMessage(
    destinationName: String,
    senderName : String,
    val communicationType : String,
    val communication : String,
) : PotMessage(destinationName, senderName, PotMessageType.COMMUNICATION) {

    override fun toString(): String {
        return "CommunicationMessage(communicationType='$communicationType', communication='$communication') ${super.toString()}"
    }
}