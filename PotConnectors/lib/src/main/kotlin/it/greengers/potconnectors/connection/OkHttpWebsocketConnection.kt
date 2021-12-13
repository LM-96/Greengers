package it.greengers.potconnectors.connection

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.util.network.*
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.utils.withExceptionToError
import it.greengers.potconnectors.utils.withLoggedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.*
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.loggerOf
import java.net.SocketAddress
import kotlin.reflect.KSuspendFunction1

class OkHttpWebsocketConnection(
    override val destinationName: String,
    private val path : String,
) : AbstractPotConnection() {

    companion object {
        val LOGGER = loggerOf(this::class.java)
    }

    val CLIENT = OkHttpClient()
    private var WEBSOCKETLISTENER : OkHttpPotWebsocketListener? = null
    var CHANNEL = Channel<Unit>()


    override suspend fun doConnect(address: SocketAddress): Error? {
        return withExceptionToError {
            val request = Request
                .Builder()
                .url("ws://$address:${address.port}/$path")
                .build()
            WEBSOCKETLISTENER = OkHttpPotWebsocketListener(this, CHANNEL, LOGGER, onMessage)
            CLIENT.newWebSocket(request, WEBSOCKETLISTENER!!)
            runBlocking { CHANNEL.receive() }
        }
    }

    override suspend fun doDisconnect(reason: String): Error? {
        var err : Error? = null
        err = WEBSOCKETLISTENER?.close()
        WEBSOCKETLISTENER = null

        return err
    }

    override val type = PotConnectionType.OKHTTP_WEBSOCKET
    override suspend fun sendAsyncMessage(msg: PotMessage): Error? {
        var err : Error? = null
        err = WEBSOCKETLISTENER?.send(msg) ?: return Error("Not connected")
        LOGGER.info("OkHttpWebsocketPotConnection[$connectedAddress] | Sent message [$msg]")

        return err
    }

}

private class OkHttpPotWebsocketListener(
    private val conn : PotConnection,
    private val ACK : Channel<Unit>,
    private val logger : KotlinLogger,
    private val callback : MutableList<KSuspendFunction1<PotMessage, Unit>>)
    : WebSocketListener() {

    private val gson = Gson()
    private var WEBSOCKET : WebSocket? = null

    override fun onOpen(webSocket: WebSocket, response: Response) {
        WEBSOCKET = webSocket
        runBlocking {
            ACK.send(Unit)
        }
    }

    fun send(potMessage: PotMessage) : Error? {
        if(WEBSOCKET == null)
            return Error("WebSocket not connected")

        return withExceptionToError {
            WEBSOCKET!!.send(gson.toJson(potMessage))
        }
    }

    fun close() : Error? {
        return withExceptionToError {
            WEBSOCKET?.close(1000, null)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        runBlocking {
            logger.info("OkHttpWebsocketListener[${conn.getConnectedAdress()}] | Received raw message: $text")
            try {
                val msg = gson.fromJson(text, PotMessage::class.java)

                callback.forEach {
                    withLoggedException(logger, "Error invoking onMessage callback method") {
                        it.invoke(msg)
                    }
                }
            } catch(je : JsonSyntaxException) {
                WebsocketPotConnection.LOGGER.warn("WebsocketPotConnection[${conn.getConnectedAdress()}] | Error in JSON parsing for message $text")
                conn.sendAsyncError(je)
            }
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        WEBSOCKET = null
    }

}