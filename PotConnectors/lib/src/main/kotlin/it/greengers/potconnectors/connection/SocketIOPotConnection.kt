package it.greengers.potconnectors.connection

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.utils.*
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.loggerOf
import java.io.*
import java.net.Socket
import java.net.SocketAddress

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class SocketIOPotConnection(
    override val destinationName: String,
    private val scope : CoroutineScope = GlobalScope
) : AbstractPotConnection() {

    companion object {
        @JvmStatic val LOGGER = loggerOf(this::class.java)
    }

    constructor(destinationName: String, socket : Socket, scope : CoroutineScope = GlobalScope) : this(destinationName, scope) {
        connectedAddress = socket.remoteSocketAddress
        this.socket = socket
        this.input = BufferedReader(InputStreamReader(socket!!.getInputStream()))
        this.output = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
        this.job = launchListener()

    }

    var job : Job? = null
    var socket : Socket? = null
    var input : BufferedReader? = null
    var output : BufferedWriter? = null
    private val gson = Gson()
    override val type: PotConnectionType = PotConnectionType.SOCKET_IO

    override suspend fun doConnect(address: SocketAddress): Error? {
        return withExceptionToError(LOGGER) {

            LOGGER.info("Connecting to [$address]")
            socket = Socket()
            socket!!.connect(address)
            input = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            output = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))

            job = launchListener()
        }
    }

    override suspend fun doDisconnect(reason : String): Error? {
        val err = withExceptionToError(LOGGER) {
            LOGGER.info("Disconnecting from  [$connectedAddress]")
            if(!socket!!.isClosed) {
                socket?.shutdownOutput()
                socket?.shutdownOutput()
                socket?.close()
            }
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
            output?.writeLineAndFlush(gson.toJson(msg))?: return Error("Not connected")
            LOGGER.info("Sent message [$msg]")
        }
    }



    private fun launchListener() : Job{
        return scope.launch(Dispatchers.IO) {
            try {
                val gson = Gson()
                var msg: PotMessage
                var rawMsg : String? = ""
                LOGGER.info("Listener for [$connectedAddress] started")

                do {
                    try {
                        rawMsg = input!!.readLine()
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
}