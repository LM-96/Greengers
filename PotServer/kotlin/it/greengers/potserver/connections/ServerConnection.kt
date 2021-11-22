package it.greengers.potserver.connections

import it.greengers.potconnectors.connection.KtorPotConnection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi

object ServerConnection {

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    val potConnection = KtorPotConnection(GlobalScope,  "main-server")

}