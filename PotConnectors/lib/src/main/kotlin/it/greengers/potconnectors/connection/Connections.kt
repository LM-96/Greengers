package it.greengers.potconnectors.connection

import com.google.gson.Gson
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.dns.PotDNS
import it.greengers.potconnectors.messages.CommunicationMessage
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.messages.PotMessageType
import it.greengers.potconnectors.messages.buildErrorMessage
import it.greengers.potconnectors.utils.FunResult
import kotlinx.coroutines.*

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

fun io.ktor.network.sockets.Socket.identificateAndThen(
    scope : CoroutineScope = AbstractPotConnection.SCOPE,
    dns : PotDNS = PotDNS.SYSTEM_DNS,
    onIdentificated : (name : String) -> Unit) {

    scope.launch {

        val input = openReadChannel()
        val output = openWriteChannel()
        val gson = Gson()
        var identificated = false

        var msg : PotMessage
        while(!identificated) {
            msg = gson.fromJson(input.readUTF8Line(), PotMessage::class.java)
            if(msg.type == PotMessageType.COMMUNICATION) {
                msg as CommunicationMessage
                if(msg.communicationType == "whoami") {
                    dns.registerOrUpdate(msg.communication, remoteAddress)
                    onIdentificated.invoke(msg.communication)
                    identificated = true
                }
            } else {
                val res = buildErrorMessage("Please send your name", "unknown-dest-name")
                output.writeStringUtf8(gson.toJson(res))
            }
        }

    }
}