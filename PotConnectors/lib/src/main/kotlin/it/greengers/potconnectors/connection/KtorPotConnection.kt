package it.greengers.potconnectors.connection

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import it.greengers.potconnectors.messages.ActorMessage
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.messages.StateReplyMessage
import it.greengers.potconnectors.messages.UnsupportedOperationMessage
import it.greengers.potconnectors.utils.FunResult
import kotlinx.coroutines.*
import java.net.SocketAddress

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class KtorPotConnection(
    private val scope : CoroutineScope = GlobalScope,
    override val destinationName: String
) : AbstractPotConnection() {

    private var job : Job? = null
    private var socket : Socket? = null
    private var input : ByteReadChannel? = null
    private var output : ByteWriteChannel? = null

    override suspend fun doConnect(address: SocketAddress): Error? {
        try {
            socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(address)
            input = socket!!.openReadChannel()
            output = socket!!.openWriteChannel(autoFlush = true)

            job = scope.launch {
                try {
                    val gson = Gson()
                    while (input != null) {
                        onMessage.invoke(gson.fromJson(input!!.readUTF8Line(), PotMessage::class.java))
                    }
                } catch (je : JsonSyntaxException) {
                    sendAsyncError(je)
                } catch (e : Exception) {}
            }
        } catch (e : Exception) {
            return Error(e)
        }
    }

    override suspend fun doDisconnect(): Error? {
        return try {
            socket?.close()
            job?.cancel()
            input = null
            output = null
            null
        } catch (e : Exception) {
            Error(e)
        }
    }

    override suspend fun sendAsyncMessage(msg: PotMessage): Error? {
        TODO("Not yet implemented")
    }

    override suspend fun performStateRequest(): FunResult<StateReplyMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun performRawActorRequest(msg: String): FunResult<ActorMessage> {
        TODO("Not yet implemented")
    }

}