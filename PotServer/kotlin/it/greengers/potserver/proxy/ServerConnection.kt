package it.greengers.potserver.proxy

import java.net.SocketAddress

interface ServerConnection {


    var connectedAddress : SocketAddress

    suspend fun connect(address : String, port : Int) : Error?
    suspend fun disconnect() : Error?

    suspend fun sendMessage(msg : ServerMessage) : Error?
    suspend fun sendCommunication(communicationType : String, communication : String) : Error?
    suspend fun sendRawActorMessage(msg : String) : Error?
    suspend fun sendActorMessage(actor : String, msgType : String, vararg payloadArgs : String) : Error?
    suspend fun sendStateRequest() : Error?
    suspend fun sendStateReply(temperature : Double, humidity : Double, brightness : Double, battery : Double) : Error?
    suspend fun sendValueOutOfRange(valueType : String, value : Any)


}