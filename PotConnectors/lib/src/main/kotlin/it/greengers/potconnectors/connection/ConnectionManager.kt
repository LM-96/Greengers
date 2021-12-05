package it.greengers.potconnectors.connection

import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.dns.PotDNS
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.utils.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.loggerOf
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A singleton that manage the PotConnection opened by the application.
 */
object ConnectionManager {

    @JvmStatic val LOGGER = loggerOf(this::class.java)

    private val CONNECTIONS = mutableMapOf<String, PotConnection>()
    private val SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this.javaClass.simpleName))

    init {
        Runtime.getRuntime().addShutdownHook(
            thread {
                runBlocking {
                    withLoggedException(LOGGER) {
                        CONNECTIONS.values.forEach {
                            it.disconnect("Closing application")
                            LOGGER.info("Closed connection [${it}]")
                        }
                    }
                }
            }
        )
    }

    /**
     * Request a new connection to a given name using a dns.
     *
     * @param destinationName the name of the destination
     * @param type the type of the connection (if not specified, it will be used the defult type)
     * @param dns the dns used to resolve the *destinationName* (if not specified it will be used the LocalDNS)
     * @param scope the scope the connection will use (if not specified it will be used the default scope)
     *
     * @return a FunResult containing the opened connection or an error if fails
     */
    suspend fun requestConnection(destinationName : String, type : PotConnectionType = PotConnectionType.KTOR, dns: PotDNS = LocalPotDNS, scope : CoroutineScope = SCOPE) : FunResult<PotConnection> {
        val res : FunResult<PotConnection>  = when(type) {
            PotConnectionType.KTOR -> newConnectedKtorConnection(destinationName, dns, scope)
            PotConnectionType.SOCKET_IO -> newConnectedSocketIOConnection(destinationName, dns, scope)
            else -> return FunResult.fromErrorString("Requested unsupported type connection")
        }

        res.withError {
            LOGGER.error("Unable to open connection to $destinationName:\n${it.stackTraceToString()}")
        }.withValue {
            CONNECTIONS[destinationName] = it
            LOGGER.info("Opened connection [$it]")
        }

        return res
    }

    /**
     * Request to disconnect the opened connection to the specified name.
     * If no connection is opened, nothing will be done
     *
     * @param destinationName the name of the end-point of the connection
     * @return an error if something fails or null if successfull
     */
    suspend fun requestDisconnection(destinationName: String) : Error? {
        val conn = CONNECTIONS.remove(destinationName)
        withNullValue(conn) {
            LOGGER.info("No opened connection for $destinationName")
            return null
        }
        return conn!!.disconnect()
    }

    /**
     * Find and return for previously opened connection
     *
     * @param destinationName the name associated to the connection
     * @return an Optional containing the connection if previously opened
     */
    fun getConnection(destinationName: String) : Optional<PotConnection> {
        return Optional.ofNullable(CONNECTIONS[destinationName])
    }

    /**
     * Fast operation to send a PotMessage using a previously opened connection.
     * The name of the end-point is taken from the message
     *
     * @param message the message to be sent
     * @return an error if something fails or null if successfull
     */
    suspend fun sendAsync(message : PotMessage) : Error? {
        val conn = CONNECTIONS[message.destinationName]
        withNullValue(conn) {
            LOGGER.info("Unable to perform a send: no opened connection for ${message.destinationName}")
        }

        return conn!!.sendAsyncMessage(message)
    }

    /**
     * Wait for a message from a previously opened connection.
     * If no connection was previouslyy opened for this name,
     * an error is returned
     *
     * @param name the name of the end-point of the connection
     * @return the message received if successfull or an error if something fails
     */
    suspend fun receiveFrom(name: String) : FunResult<PotMessage> {
        val conn = CONNECTIONS[name] ?: return FunResult(Error("No opened connection for [$name]"))
        val listener = SingleMessageListener(conn)
        return listener.waitMessage()
    }

}