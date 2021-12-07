package it.greengers.potconnectors.connection

import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.dns.PotDNS
import it.greengers.potconnectors.utils.FunResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun newKtorConnection(destinationName : String, scope : CoroutineScope = GlobalScope) : PotConnection {
    return KtorPotConnection(destinationName, scope)
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun newSocketIOConnection(destinationName: String, scope: CoroutineScope = GlobalScope) : PotConnection {
    return SocketIOPotConnection(destinationName, scope)
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
suspend fun newConnectedKtorConnection(destinationName : String, dns: PotDNS = LocalPotDNS, scope : CoroutineScope = GlobalScope) : FunResult<PotConnection> {
    val addr = dns.resolve(destinationName)
    if(addr.thereIsError())
        return FunResult(addr.error!!)

    val conn = KtorPotConnection(destinationName, scope)
    val connErr = conn.connect(dns) ?: return FunResult(conn)

    return FunResult(connErr)
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
suspend fun newConnectedSocketIOConnection(destinationName : String, dns: PotDNS = LocalPotDNS, scope : CoroutineScope = GlobalScope) : FunResult<PotConnection> {
    val addr = dns.resolve(destinationName)
    if(addr.thereIsError())
        return FunResult(addr.error!!)

    val conn = SocketIOPotConnection(destinationName, scope)
    val connErr = conn.connect(dns) ?: return FunResult(conn)

    return FunResult(connErr)
}