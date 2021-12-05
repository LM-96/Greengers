package it.greengers.potconnectors.connection

import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.dns.PotDNS
import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.FunResult
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.ApplMessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.net.SocketAddress
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KSuspendFunction1

interface PotConnection {

    val destinationName : String
    val type : PotConnectionType

    /**
     * Connect this PotConnection to a specified IP and port
     *
     * @param ip the IP address of the other end-point
     * @param port the port you want to connect
     *
     * @return a null error if the connection was successful or a non-null
     * error otherwise
     */
    suspend fun connect(ip : String, port : Int) : Error?

    /**
     * Connect this PotConnection using a DNS to resolve the destination
     * IP and PORT. If a PotDNS is not specified, the LocalPotDNS will be used
     *
     * @param dns the PotDNS implementation that must be used to resolve the destination
     * name
     *
     * @return a null error if the connection was successful or a non-null
     * error otherwise
     */
    suspend fun connect(dns : PotDNS = LocalPotDNS) : Error?

    /**
     * Connect this PotConnection to a specified SocketAddress
     *
     * @param address the SocketAddress of the other end-point
     *
     * @return a null error if the connection was successful or a non-null
     * error otherwise
     */
    suspend fun connect(address : SocketAddress) : Error?

    /**
     * Return true if this connection is already connected with the destinationName
     *
     * @return true if connected, false otherwise
     */
    suspend fun isConnected() : Boolean

    /**
     * Return the connected SocketAddres
     *
     * @return the connected SocketAddress or null if not connected
     */
    suspend fun getConnectedAdress() : SocketAddress?

    /**
     * Disconnect this connection
     *
     * @param reason the reason of the disconnection
     * @return a not null error if fails, null if successful disconnected
     */
    suspend fun disconnect(reason : String = "Requested disconnection") : Error?

    /**
     * Add an action that will be performed when a message is received.
     *
     * @param callback the action to be invoked when a message is received
     */
    suspend fun addCallbackOnMessage(callback: KSuspendFunction1<PotMessage, Unit>)

    /**
     * Remove a callback that was previously attached when a message is received
     *
     * @param callback the callback to be detached
     */
    suspend fun removeCallbackOnMessage(callback: KSuspendFunction1<PotMessage, Unit>)

    /**
     * Add a callback that will be invoked when this connection is disconnected
     *
     * @param the callback
     */
    suspend fun addCallbackOnDisconnection(callback : KSuspendFunction1<String, Unit>)

    /**
     * Remove a callback that was previously attached when a disconnection is performed
     *
     * @param callback the callback to be detached
     */
    suspend fun removeCallbackOnDisconnection(callback: KSuspendFunction1<String, Unit>)

    /**
     * Send a message using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param msg the message to be sent
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncMessage(msg : PotMessage) : Error?

    /**
     * Send a CommunicationMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param communicationType the type of the communication
     * @param communication a string that represents the communication
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncCommunication(communicationType : String, communication : String) : Error?

    /**
     * Send an ActorMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent.
     * If the string is not formatted as QAK-message, an error is returned
     *
     * @param msg the ApplMessage in the format specified by the QAK-System
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncRawActorMessage(msg : String) : Error?

    /**
     * Send an ActorMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param destActor the QAK-Actor name that will receive the message
     * @param msgType the ApplMessageType
     * @param msgName the QAK-Message name
     * @param payloadArgs the args of the message
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncActorMessage(destActor : String, msgType : ApplMessageType, msgName : String, vararg payloadArgs : String) : Error?

    /**
     * Send an ActorMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param applMessage the QAK ApplMessage to send
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncActorMessage(applMessage: ApplMessage) : Error?

    /**
     * Send a StateRequest using this connection. This send is asynchronous, so this function
     * return after the message has been sent.
     * Notice that the response is not caught by the invocation of this function so
     * it is needed to intercept it using a manual callback (or you should use the function
     * *performActorRequest()*)
     *
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncStateRequest() : Error?
    /**
     * Send an StateReplyMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param temperature the current temperature
     * @param humidity the current humidity
     * @param brightness the current brightness
     * @param battery the current battery
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncStateReply(temperature : Double, humidity : Double, brightness : Double, battery : Double) : Error?

    /**
     * Send a ValueOutOfRangeMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param valueType the description of the type of the critical value
     * @param value the current value
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncValueOutOfRange(valueType : String, value : Any) : Error?

    /**
     * Send an ErrorMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param errorDescription the representation of the error
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncError(errorDescription : String) : Error?

    /**
     * Send an ErrorMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param e a *Throwable* object that will be used to build the ErrorMessage
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendAsyncError(e : Throwable) : Error?

    /**
     * Send an UsupportedOperationMessage using this connection. This send is asynchronous, so this function
     * return after the message has been sent
     *
     * @param explanation the explanation for the unsupported reason
     * @param referredMessage the message that contains the unsupported request
     * @return a not null error if the message is not successfully sent or null otherwise
     */
    suspend fun sendUnsupportedOperationMessage(explanation : String, referredMessage : PotMessage? = null) : Error?

    /**
     * Perform a synchronous state request and wait for the response
     *
     * @return the FunResult containing the response or an error if something fails
     */
    suspend fun performStateRequest() : FunResult<StateReplyMessage>

    /**
     * Perform a synchronous QAK-Message request and wait for the response.
     * The message is validated and if it is not in QAK-Message format, then an error
     * is returned
     *
     * @param msg the ApplMessage in the format specified by the QAK-System
     *
     * @return the FunResult containing the response or an error if something fails
     */
    suspend fun performRawActorRequest(msg : String) : FunResult<ActorMessage>

    /**
     * Perform a synchronous QAK-Message request and wait for the response.
     *
     * @param destActor the QAK-Actor name that will receive the message
     * @param msgType the ApplMessageType
     * @param msgName the QAK-Message name
     * @param payloadArgs the args of the message
     *
     * @return the FunResult containing the response or an error if something fails
     */
    suspend fun performActorRequest(destActor : String, msgName : String, vararg payloadArgs : String) : FunResult<ActorMessage>

    /**
     * Perform a synchronous QAK-Message request and wait for the response.
     *
     * @param applMessage the QAK ApplMessage to send
     *
     * @return the FunResult containing the response or an error if something fails
     */
    suspend fun performActorRequest(applMessage : ApplMessage) : FunResult<ActorMessage>

    override fun toString() : String

}