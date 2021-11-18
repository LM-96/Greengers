package it.greengers.potconnectors.messages

class ValueOutOfRangeMessage(
    destinationName: String,
    senderName: String,
    val valueType : String,
    val value : String
) : PotMessage(destinationName, senderName, PotMessageType.VALUE_OUT_OF_RANGE) {

    override fun toString(): String {
        return "ValueOutOfRangeMessage(valueType='$valueType', value='$value') ${super.toString()}"
    }
}