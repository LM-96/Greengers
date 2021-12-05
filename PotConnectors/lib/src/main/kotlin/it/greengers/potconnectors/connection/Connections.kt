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
suspend fun newConnectedKtorConnection(destinationName : String, dns: PotDNS = LocalPotDNS, scope : CoroutineScope = GlobalScope) : FunResult<PotConnection> {
    val addr = dns.resolve(destinationName)
    if(addr.thereIsError())
        return FunResult(addr.error!!)

    val conn = KtorPotConnection(scope, destinationName)
    val connErr = conn.connect(dns) ?: return FunResult(conn)

    return FunResult(connErr)
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
suspend fun newConnectedSocketIOConnection(destinationName : String, dns: PotDNS = LocalPotDNS, scope : CoroutineScope = GlobalScope) : FunResult<PotConnection> {
    val addr = dns.resolve(destinationName)
    if(addr.thereIsError())
        return FunResult(addr.error!!)

    val conn = SocketIOPotConnection(scope, destinationName)
    val connErr = conn.connect(dns) ?: return FunResult(conn)

    return FunResult(connErr)
}