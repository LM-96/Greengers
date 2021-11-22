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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.apache.logging.log4j.kotlin.logger
import org.apache.logging.log4j.kotlin.loggerOf
import java.net.SocketAddress

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class KtorPotConnection(
    private val scope : CoroutineScope = GlobalScope,
    override val destinationName: String
) : AbstractPotConnection() {

    constructor(scope : CoroutineScope, destinationName: String, socket : Socket) : this(scope, destinationName) {
        connectedAddress = socket.remoteAddress
        this.socket = socket
        input = socket.openReadChannel()
        output = socket.openWriteChannel(autoFlush = true)
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
    private val requestUtil = StateRequestUtil(this)

    override suspend fun doConnect(address: SocketAddress): Error? {
        try {
            LOGGER.info("Connecting to [$address]")
            socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(address)
            input = socket!!.openReadChannel()
            output = socket!!.openWriteChannel(autoFlush = true)
            LOGGER.info("Connected to [$address]")

            job = launchListener()
        } catch (e : Exception) {
            LOGGER.error(e)
            return Error(e)
        }

        return null
    }

    private fun launchListener() : Job {
        return scope.launch {
            try {
                val gson = Gson()
                var msg: PotMessage
                var rawMsg : String? = ""
                LOGGER.info("Listener for [$connectedAddress] started")

                while (input != null) {
                    try {
                        rawMsg = input!!.readUTF8Line()
                        msg = gson.fromJson(rawMsg, PotMessage::class.java)
                        LOGGER.info("Received message [$msg] from [$connectedAddress]")
                        onMessage.forEach { it.invoke(msg) }
                    } catch (je : JsonSyntaxException) {
                    LOGGER.warn("Error in JSON parsing for message $rawMsg")
                    sendAsyncError(je)
                }

                }
            } catch (e : Exception) {
                LOGGER.info("Listener for [$connectedAddress] closed")
            }
        }
    }

    override suspend fun doDisconnect(): Error? {
        return try {
            LOGGER.info("Disconnecting from  [$connectedAddress]")
            socket?.close()
            socket?.dispose()
            job?.cancel()
            input = null
            output = null
            LOGGER.info("Disconnected from [$connectedAddress]")

            null
        } catch (e : Exception) {
            LOGGER.error(e)
            Error(e)
        }
    }

    override suspend fun sendAsyncMessage(msg: PotMessage): Error? {
        try {
            output?.writeStringUtf8(gson.toJson(msg))?: return Error("Not connected")
            LOGGER.info("Sent message [$msg]")
        } catch (e : Exception) {
            LOGGER.error("Error sending message [$msg]: $e")
            return Error(e)
        }
        return null
    }

    override suspend fun performStateRequest(): FunResult<StateReplyMessage> {
        return requestUtil
            .attachForSingleRequest()
            .performStateRequestAndWaitResponse()
    }

    override suspend fun performRawActorRequest(msg: String): FunResult<ActorMessage> {
        return requestUtil
            .attachForSingleRequest()
            .performActorRequest(msg)
    }

}