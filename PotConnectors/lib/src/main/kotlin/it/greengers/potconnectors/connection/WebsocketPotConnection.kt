package it.greengers.potconnectors.connection

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.network.*
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.utils.withExceptionToError
import it.greengers.potconnectors.utils.withLoggedException
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.loggerOf
import java.net.SocketAddress

class WebsocketPotConnection(
    override val destinationName: String,
    private val path : String,
    private val scope : CoroutineScope = SCOPE
) : AbstractPotConnection() {

    companion object {
        val LOGGER = loggerOf(this::class.java)
    }

    private val CLIENT = HttpClient(OkHttp) {
        install(WebSockets)
    }
    private var WEBSOCKETSESSION : WebSocketSession? = null
    private var job : Job? = null
    override val type = PotConnectionType.WEBSOCKET
    private val gson = Gson()


    private fun launchListener() {
        job = SCOPE.launch {
            try {
                val input = WEBSOCKETSESSION!!.incoming
                val gson = Gson()
                var msg: PotMessage
                var rawMsg : String? = ""
                LOGGER.info("Listener for [$connectedAddress] started")

                do {
                    try {
                        rawMsg = (input.receive() as? Frame.Text)?.readText()
                        LOGGER.info("WebsocketPotConnection[$connectedAddress] | received message: $rawMsg")
                        if(rawMsg != null) {
                            msg = gson.fromJson(rawMsg, PotMessage::class.java)
                            LOGGER.info("WebsocketPotConnection[$connectedAddress] | parsed message [$msg]")
                            onMessage.forEach {
                                withLoggedException(LOGGER, "Error invoking onMessage callback method") {
                                    it.invoke(msg)
                                }
                            }
                        }
                    } catch (je : JsonSyntaxException) {
                        LOGGER.warn("WebsocketPotConnection[$connectedAddress] | Error in JSON parsing for message $rawMsg")
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

    override suspend fun doConnect(address: SocketAddress): Error? {
        var err : Error? = null
        runBlocking {
            try {
                WEBSOCKETSESSION = CLIENT.webSocketSession(
                    method = HttpMethod.Get,
                    host = address.hostname,
                    port = address.port,
                    path = path)
                LOGGER.info("WebsocketPotConnection[$connectedAddress] | WebSocketSession started")
            } catch (e : Exception) {
                err = Error(e)
                LOGGER.error(e)
            }
        }
        launchListener()
        return err
    }

    override suspend fun doDisconnect(reason: String): Error? {
        return withExceptionToError(LOGGER) {
            job!!.cancel(reason)
            WEBSOCKETSESSION!!.cancel(reason)
        }
    }

    override suspend fun sendAsyncMessage(msg: PotMessage): Error? {
        return withExceptionToError(LOGGER) {
            WEBSOCKETSESSION?.send(gson.toJson(msg)) ?: return Error("Not connected")
            LOGGER.info("WebsocketPotConnection[$connectedAddress] | Sent message [$msg]")
        }
    }

}