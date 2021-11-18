package it.greengers.potconnectors.messages

import it.unibo.kactor.ApplMessage

class ActorMessage(
    destinationName: String,
    senderName: String,
    val rawMsg: String
) : PotMessage(destinationName, senderName, PotMessageType.ACTOR) {

    override fun toString(): String {
        return "ActorMessage(rawMsg='$rawMsg') ${super.toString()}"
    }

    fun toQakMessage() : ApplMessage {
        return ApplMessage(rawMsg)
    }
}