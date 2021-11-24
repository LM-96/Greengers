package it.greengers.potconnectors.messages

import it.unibo.kactor.ApplMessage
import kotlin.jvm.Throws

class ActorMessage(
    destinationName: String,
    senderName: String,
    val applMessage: ApplMessage
) : PotMessage(destinationName, senderName, PotMessageType.ACTOR) {

    override fun toString(): String {
        return "ActorMessage(rawMsg='${applMessage.toString()}') ${super.toString()}"
    }

    @Throws(Exception::class)
    fun toQakMessage() : ApplMessage {
        return applMessage
    }
}