package it.greengers.potcentral.handlers

import it.greengers.potconnectors.connection.PotConnection

class ClientDisconnectionHandler(potConnection: PotConnection, autoAttach : Boolean = true)
    : AbstractDisconnectionHandler(potConnection, autoAttach)
{
    override suspend fun onDisconnection(reason: String) {
        TODO("Not yet implemented")
    }
}