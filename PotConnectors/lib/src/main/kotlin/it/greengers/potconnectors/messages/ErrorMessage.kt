package it.greengers.potconnectors.messages

class ErrorMessage(
    destinationName: String,
    senderName: String,
    val errorDescription : String
) : PotMessage(destinationName, senderName, PotMessageType.ERROR){
}