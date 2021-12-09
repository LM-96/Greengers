package it.greengers.potcentral.handlers

import it.greengers.potconnectors.messages.PotMessage

interface DisconnectionHandler {

    suspend fun attachOnDisconnection()
    suspend fun detachOnDisconnection()

    suspend fun onDisconnection(reason : String)

}