package it.greengers.potconnectors.connection

import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.FunResult
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.ApplMessageType
import it.unibo.kactor.MsgUtil
import java.net.InetSocketAddress
import java.net.SocketAddress

abstract class AbstractPotConnection : PotConnection {

    private var connectedAddress : SocketAddress? = null
    override var onMessage: ((PotMessage) -> Unit) = {}

    abstract suspend fun doConnect(address: SocketAddress) : Error?
    abstract suspend fun doDisconnect() : Error?

    override suspend fun connect(ip: String, port: Int): Error? {
        return try {
            connect(InetSocketAddress(ip, port))
        } catch (e : Exception) {
            Error(e)
        }
    }

    override suspend fun connect(address: SocketAddress): Error? {
        if(connectedAddress != null)
            return Error("Already connected. Please disconnect before")

        return try {
            val res = doConnect(address)
            if(res == null)
                connectedAddress = address

            res
        } catch (e : Exception) {
            Error(e)
        }
    }

    override suspend fun disconnect(): Error? {
        if(connectedAddress != null) {
            return try {
                val res = doDisconnect()
                if(res == null)
                    connectedAddress = null

                res
            } catch (e : Exception) {
                Error(e)
            }
        }

        return null
    }

    override suspend fun isConnected(): Boolean {
        return connectedAddress != null
    }

    override suspend fun getConnectedAdress(): SocketAddress? {
        return connectedAddress
    }

    override suspend fun sendAsyncActorMessage(
        destActor: String,
        msgType: ApplMessageType,
        msgName: String,
        vararg payloadArgs: String
    ): Error? {
        val content = "$msgName(${payloadArgs.map { it.trim() }.joinToString(separator = ",")})"
        val applMsg : ApplMessage? = when(msgType) {
            ApplMessageType.dispatch ->
                MsgUtil.buildDispatch(InfoPoint.getApplicationHostName(), msgName, content, destActor)
            ApplMessageType.event ->
                MsgUtil.buildEvent(InfoPoint.getApplicationHostName(), msgName, content)
            ApplMessageType.request ->
                MsgUtil.buildRequest(InfoPoint.getApplicationHostName(), msgName, content, destActor)
            ApplMessageType.reply ->
                MsgUtil.buildReply(InfoPoint.getApplicationHostName(), msgName, content, destActor)

            else -> {return Error("Unsupported ApplMessageType")}
        }

        return sendAsyncMessage(ActorMessage(destinationName, InfoPoint.getApplicationHostName(), applMsg.toString()))
    }

    override suspend fun sendAsyncRawActorMessage(msg: String): Error? {
        return try {
            sendAsyncMessage(ActorMessage(destinationName, InfoPoint.getApplicationHostName(), ApplMessage(msg).toString()))
        } catch (e : Exception) {
            Error("Bad actor message format")
        }
    }

    override suspend fun sendAsyncCommunication(communicationType: String, communication: String): Error? {
        return sendAsyncMessage(CommunicationMessage(destinationName, InfoPoint.getApplicationHostName(), communicationType, communication))
    }

    override suspend fun sendAsyncStateReply(
        temperature: Double,
        humidity: Double,
        brightness: Double,
        battery: Double
    ): Error? {
        return sendAsyncMessage(StateReplyMessage(destinationName, InfoPoint.getApplicationHostName(), temperature, humidity, brightness, battery))
    }

    override suspend fun sendAsyncStateRequest(): Error? {
        return sendAsyncMessage(StateRequestMessage(destinationName, InfoPoint.getApplicationHostName()))
    }

    override suspend fun sendAsyncValueOutOfRange(valueType: String, value: Any) : Error? {
        return sendAsyncMessage(ValueOutOfRangeMessage(destinationName, InfoPoint.getApplicationHostName(), valueType, value.toString()))
    }

    override suspend fun sendAsyncError(errorDescription: String) : Error? {
        return sendAsyncMessage(ErrorMessage(destinationName, InfoPoint.getApplicationHostName(), errorDescription))
    }

    override suspend fun sendAsyncError(e: Throwable): Error? {
        return sendAsyncError(e.localizedMessage)
    }

    override suspend fun performActorRequest(
        destActor: String,
        msgName: String,
        vararg payloadArgs: String
    ): FunResult<ActorMessage> {
        val content = "$msgName(${payloadArgs.map { it.trim() }.joinToString(separator = ",")})"
        return try {
            performRawActorRequest(MsgUtil.buildRequest(InfoPoint.getApplicationHostName(), msgName, content, destActor).toString())
        } catch (e : Exception) {
            FunResult(error = Error("Bad actor message format"))
        }
    }

}