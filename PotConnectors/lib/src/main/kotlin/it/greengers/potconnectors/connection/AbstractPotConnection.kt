package it.greengers.potconnectors.connection

import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.dns.PotDNS
import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.*
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.ApplMessageType
import it.unibo.kactor.MsgUtil
import java.net.InetSocketAddress
import java.net.SocketAddress
import kotlin.reflect.KSuspendFunction1

abstract class AbstractPotConnection : PotConnection {

    protected var connectedAddress : SocketAddress? = null
    val onMessage: MutableList<KSuspendFunction1<PotMessage, Unit>> = mutableListOf()

    abstract suspend fun doConnect(address: SocketAddress) : Error?
    abstract suspend fun doDisconnect() : Error?

    override suspend fun connect(ip: String, port: Int): Error? {
        return withExceptionToError { connect(InetSocketAddress(ip, port)) }
    }

    override suspend fun connect(dns: PotDNS): Error? {
        val address = dns.resolve(destinationName)
        return if(address.thereIsError())
            address.error
        else
            connect(address.res!!)
    }

    override suspend fun addCallbackOnMessage(callback: KSuspendFunction1<PotMessage, Unit>) {
        onMessage.add(callback)
    }

    override suspend fun removeCallbackOnMessage(callback: KSuspendFunction1<PotMessage, Unit>) {
        onMessage.remove(callback)
    }

    override suspend fun connect(address: SocketAddress): Error? {
        if(connectedAddress != null)
            return Error("Already connected. Please disconnect before")

        return withExceptionAndErrorToError {
            val res = doConnect(address)
            if(res == null)
                connectedAddress = address

            res
        }
    }

    override suspend fun disconnect(): Error? {
        if(connectedAddress != null) {
            withExceptionAndErrorToError {
                val res = doDisconnect()
                if(res == null)
                    connectedAddress = null

                res
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
        val applMsg = buildApplMessage(destActor, msgType, msgName)
        if(applMsg.thereIsError())
            return applMsg.error

        return sendAsyncMessage(buildActorMessage(applMsg.res!!, destinationName))
    }

    override suspend fun sendAsyncActorMessage(applMessage: ApplMessage): Error? {
        return sendAsyncMessage(buildActorMessage(applMessage, destinationName))
    }

    override suspend fun sendAsyncRawActorMessage(msg: String): Error? {
        val actorMsg = buildActorMessage(msg, destinationName)
        if(actorMsg.thereIsError())
            return actorMsg.error

        return sendAsyncMessage(actorMsg.res!!)
    }

    override suspend fun sendAsyncCommunication(communicationType: String, communication: String): Error? {
        return sendAsyncMessage(buildCommunicationMessage(communicationType, communication, destinationName))
    }

    override suspend fun sendAsyncStateReply(
        temperature: Double,
        humidity: Double,
        brightness: Double,
        battery: Double
    ): Error? {
        return sendAsyncMessage(buildStateReplyMessage(temperature, humidity, brightness, battery, destinationName))
    }

    override suspend fun sendAsyncStateRequest(): Error? {
        return sendAsyncMessage(buildStateRequestMessage(destinationName))
    }

    override suspend fun sendAsyncValueOutOfRange(valueType: String, value: Any) : Error? {
        return sendAsyncMessage(buildValueOutOfRangeMessage(valueType, value, destinationName))
    }

    override suspend fun sendAsyncError(errorDescription: String) : Error? {
        return sendAsyncMessage(buildErrorMessage(errorDescription, destinationName))
    }

    override suspend fun sendAsyncError(e: Throwable): Error? {
        return sendAsyncMessage(buildErrorMessage(e, destinationName))
    }

    override suspend fun sendUnsupportedOperationMessage(explanation: String, referredMessage: PotMessage?) : Error? {
        return sendAsyncMessage(buildUnsupportedOperationMessage(explanation, referredMessage, destinationName))
    }

    override suspend fun performActorRequest(
        destActor: String,
        msgName: String,
        vararg payloadArgs: String
    ): FunResult<ActorMessage> {
        val applMsg = buildApplMessage(destActor, ApplMessageType.request, msgName, *payloadArgs)
        if(applMsg.thereIsError()) {
            return applMsg.castWithError()
        }

        return performActorRequest(applMsg.res!!)

        /*
        buildApplMessage(destActor, ApplMessageType.request, msgName, *payloadArgs)
            .withThisIfError { return it.castWithError() }
            .withValue { return performActorRequest(it) }*/
    }

    override suspend fun performRawActorRequest(msg: String): FunResult<ActorMessage> {
        val applMsg = buildValidApplMessage(msg)
        if(applMsg.thereIsError())
            return applMsg.castWithError()

        return performActorRequest(applMsg.res!!)
    }

}