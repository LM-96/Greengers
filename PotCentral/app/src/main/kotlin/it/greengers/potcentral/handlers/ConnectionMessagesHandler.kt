package it.greengers.potcentral.handlers

import it.greengers.potconnectors.messages.PotMessage

interface ConnectionMessagesHandler {

    suspend fun attachOnMessage()
    suspend fun detachOnMessage()

    suspend fun onMessage(msg : PotMessage)

}