package it.greengers.potserver.proxy

import com.google.gson.Gson
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress

class KtorServerConnection {

    private var socket : Socket? = null
    private var input : ByteReadChannel? = null
    private var output : ByteWriteChannel? = null
    private val gson = Gson()

    suspend fun connect(address : String, port : Int) : Error? {
        try {
            aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(InetSocketAddress(address, port))
        } catch (e : Exception) {
            return Error(e)
        }
        input = socket?.openReadChannel()
        output = socket?.openWriteChannel()

        return null
    }

    suspend fun sendMessage(msg : ServerMessage) : Error? {
        try {
            output?.writeStringUtf8(gson.toJson(msg))
        } catch (e : Exception) {
            return Error(e)
        }
        return null
    }

}