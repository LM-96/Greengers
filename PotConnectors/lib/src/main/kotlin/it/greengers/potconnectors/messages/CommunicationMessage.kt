package it.greengers.potconnectors.messages

import java.util.*

class CommunicationMessage(
    destinationName: String,
    senderName : String,
    val communicationType : String,
    val communication : String,
) : PotMessage(destinationName, senderName, PotMessageType.COMMUNICATION) {

    override fun toString(): String {
        return "CommunicationMessage(communicationType='$communicationType', communication='$communication') ${super.toString()}"
    }

    fun isBuiltInCommunicationType() : Optional<BuiltInCommunicationType> {
        return try {
            Optional.of(BuiltInCommunicationType.valueOf(communicationType))
        } catch (e : Exception) {
            Optional.empty()
        }
    }
}