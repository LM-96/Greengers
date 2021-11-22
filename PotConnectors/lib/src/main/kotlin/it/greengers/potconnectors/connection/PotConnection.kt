package it.greengers.potconnectors.connection

import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.FunResult
import it.unibo.kactor.ApplMessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.net.SocketAddress
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KSuspendFunction1

interface PotConnection {

    val destinationName : String

    suspend fun connect(ip : String, port : Int) : Error?
    suspend fun connect(address : SocketAddress) : Error?
    suspend fun isConnected() : Boolean
    suspend fun getConnectedAdress() : SocketAddress?
    suspend fun disconnect() : Error?
    suspend fun addCallbackOnMessage(callback: KSuspendFunction1<PotMessage, Unit>)
    suspend fun removeCallbackOnMessage(callback: KSuspendFunction1<PotMessage, Unit>)

    suspend fun sendAsyncMessage(msg : PotMessage) : Error?
    suspend fun sendAsyncCommunication(communicationType : String, communication : String) : Error?
    suspend fun sendAsyncRawActorMessage(msg : String) : Error?
    suspend fun sendAsyncActorMessage(destActor : String, msgType : ApplMessageType, msgName : String, vararg payloadArgs : String) : Error?
    suspend fun sendAsyncStateRequest() : Error?
    suspend fun sendAsyncStateReply(temperature : Double, humidity : Double, brightness : Double, battery : Double) : Error?
    suspend fun sendAsyncValueOutOfRange(valueType : String, value : Any) : Error?
    suspend fun sendAsyncError(errorDescription : String) : Error?
    suspend fun sendAsyncError(e : Throwable) : Error?

    suspend fun performStateRequest() : FunResult<StateReplyMessage>
    suspend fun performRawActorRequest(msg : String) : FunResult<ActorMessage>
    suspend fun performActorRequest(destActor : String, msgName : String, vararg payloadArgs : String) : FunResult<ActorMessage>

}