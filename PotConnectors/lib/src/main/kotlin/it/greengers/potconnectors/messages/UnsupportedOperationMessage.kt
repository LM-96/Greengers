package it.greengers.potconnectors.messages

class UnsupportedOperationMessage(
    destinationName: String,
    senderName: String,
    val explanation : String? = null,
    val referredMessage : PotMessage? = null,
) : PotMessage(destinationName, senderName, PotMessageType.UNSUPPORTED_OPERATION) {
}