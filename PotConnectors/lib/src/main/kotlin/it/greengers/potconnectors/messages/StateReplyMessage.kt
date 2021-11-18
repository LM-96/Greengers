package it.greengers.potconnectors.messages

class StateReplyMessage(
    destinationName: String,
    senderName: String,
    val temperature : Double,
    val humidity : Double,
    val brightness : Double,
    val battery : Double
) : PotMessage(destinationName, senderName, PotMessageType.STATE_REPLY) {

    override fun toString(): String {
        return "StateReplyMessage(temperature=$temperature, humidity=$humidity, brightness=$brightness, battery=$battery) ${super.toString()}"
    }
}