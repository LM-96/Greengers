package it.greengers.potconnectors.connection

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import it.greengers.potconnectors.messages.ActorMessage
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.messages.StateReplyMessage
import it.greengers.potconnectors.utils.FunResult
import it.greengers.potconnectors.utils.StateRequestUtil
import it.greengers.potconnectors.utils.withExceptionToError
import it.greengers.potconnectors.utils.withLoggedException
import it.unibo.kactor.ApplMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.logging.log4j.kotlin.logger
import org.apache.logging.log4j.kotlin.loggerOf
import java.net.SocketAddress
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class KtorPotConnection(
    override val destinationName: String,
    private val scope : CoroutineScope = SCOPE
) : AbstractPotConnection() {

    constructor(destinationName: String, socket : Socket,
                input: ByteReadChannel = socket.openReadChannel(),
                output: ByteWriteChannel = socket.openWriteChannel(autoFlush = true),
                scope : CoroutineScope = SCOPE
    ) : this(destinationName, scope) {
        connectedAddress = socket.remoteAddress
        this.socket = socket
        this.input = input
        this.output = output
        job = launchListener()
    }

    companion object {
        @JvmStatic val LOGGER = loggerOf(this::class.java)
    }

    private var job : Job? = null
    private var socket : Socket? = null
    private var input : ByteReadChannel? = null
    private var output : ByteWriteChannel? = null
    private val gson = Gson()
    override val type: PotConnectionType = PotConnectionType.KTOR

    override suspend fun doConnect(address: SocketAddress): Error? {
        return withExceptionToError(LOGGER) {
            LOGGER.info("Connecting to [$address]")
            socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(address)
            input = socket!!.openReadChannel()
            output = socket!!.openWriteChannel(autoFlush = true)
            LOGGER.info("Connected to [$address]")

            job = launchListener()
        }
    }

    private fun launchListener() : Job {
        return scope.launch {
            try {
                val gson = Gson()
                var msg: PotMessage
                var rawMsg : String? = ""
                LOGGER.info("Listener for [$connectedAddress] started")

                do {
                    try {
                        rawMsg = input!!.readUTF8Line()
                        msg = gson.fromJson(rawMsg, PotMessage::class.java)
                        LOGGER.info("Received message [$msg] from [$connectedAddress]")
                        onMessage.forEach {
                            withLoggedException(LOGGER, "Error invoking onMessage callback method") {
                                it.invoke(msg)
                            }
                        }
                    } catch (je : JsonSyntaxException) {
                        LOGGER.warn("Error in JSON parsing for message $rawMsg")
                        sendAsyncError(je)
                    }
                } while (rawMsg != null)

                //The cycle ends becaus null is received -> client has naturally close connection
                LOGGER.info("Client [$connectedAddress] has closed connection")
                disconnect("Client [$connectedAddress] has closed connection")

            } catch (e : Exception) {
                //Exception while reading from socket channel
                LOGGER.info("Listener for [$connectedAddress] closed")
                LOGGER.error(e)
                disconnect(e.stackTraceToString())
            }
        }
    }

    override suspend fun doDisconnect(reason : String): Error? {
        val err = withExceptionToError(LOGGER) {
            LOGGER.info("Disconnecting from  [$connectedAddress]")
            socket?.close()
            socket?.dispose()
            job?.cancel()
            input = null
            output = null
            LOGGER.info("Disconnected from [$connectedAddress]")
        }

        onDisconnection.forEach {
            withLoggedException(LOGGER, "Error invoking onDisconnection callback method") {
                it.invoke(reason)
            }
        }
        return err
    }

    override suspend fun sendAsyncMessage(msg: PotMessage): Error? {
        return withExceptionToError(LOGGER) {
            output?.writeStringUtf8(gson.toJson(msg))?: return Error("Not connected")
            LOGGER.info("Sent message [$msg]")
        }
    }

}