package it.greengers.potconnectors.messages

class StateRequestMessage(
    destinationName: String,
    senderName: String) : PotMessage(destinationName, senderName, PotMessageType.STATE_REQUEST) {

    override fun toString(): String {
        return "StateRequestMessage() ${super.toString()}"
    }
}