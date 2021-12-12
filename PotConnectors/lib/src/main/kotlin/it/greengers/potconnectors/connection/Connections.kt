package it.greengers.potconnectors.connection

import com.google.gson.Gson
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.dns.PotDNS
import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.FunResult
import it.greengers.potconnectors.utils.toFunResult
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.logger
import java.util.*

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

val POTCONNECT_LOGGER = logger("PotConnect")

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun io.ktor.network.sockets.Socket.potConnect(
    scope : CoroutineScope = AbstractPotConnection.SCOPE,
    dns : PotDNS = PotDNS.SYSTEM_DNS,
    onIdentificated : suspend (connection : FunResult<PotConnection>) -> Unit = {}) {

    POTCONNECT_LOGGER.info("PotConnect[$remoteAddress] | Begin PotConnect")
    val sock = this
    val addr = remoteAddress
    scope.launch {

        try {
            val input = openReadChannel()
            val output = openWriteChannel()
            val gson = Gson()
            var identificated = false

            var msg : PotMessage
            var commType : Optional<BuiltInCommunicationType>

            while(!identificated) {
                POTCONNECT_LOGGER.info("PotConnect[$addr] | Waiting for identification message")
                msg = gson.fromJson(input.readUTF8Line(), PotMessage::class.java)
                POTCONNECT_LOGGER.info("PotConnect[$addr] | Received message [$msg] from [")
                if(msg.type == PotMessageType.COMMUNICATION) {
                    commType = (msg as CommunicationMessage).isBuiltInCommunicationType()
                    if(commType.isPresent) {
                        if(commType.get() == BuiltInCommunicationType.WHOAMI) {
                            POTCONNECT_LOGGER.info("PotConnect[$addr] | Found identification message. HostName: ${msg.communication}")
                            dns.registerOrUpdate(msg.communication, remoteAddress)
                            onIdentificated.invoke(KtorPotConnection(msg.communication, sock, input, output).toFunResult())
                            identificated = true
                            POTCONNECT_LOGGER.info("PotConnect[$addr] | Identification completed")
                        }
                    }
                } else {
                    POTCONNECT_LOGGER.error("PotConnect[$addr] | Host MUST sent his name before")
                    val res = buildErrorMessage("Please send your name", "unknown-dest-name")
                    output.writeStringUtf8(gson.toJson(res))
                }
            }
        } catch (e : Exception) {
            onIdentificated.invoke(FunResult.fromErrorString("Error while identificating: ${e.localizedMessage}"))
        }

    }
}