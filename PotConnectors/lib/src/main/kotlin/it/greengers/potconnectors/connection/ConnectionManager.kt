package it.greengers.potconnectors.connection

import io.ktor.utils.io.*
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.dns.PotDNS
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.utils.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.kotlin.loggerOf
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A singleton that manage the PotConnection opened by the application.
 */
object ConnectionManager {

    @JvmStatic val LOGGER = loggerOf(this::class.java)

    @JvmStatic private val CONNECTIONS = mutableMapOf<String, PotConnection>()
    @JvmStatic private val MUTEX = Mutex()
    private val SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this.javaClass.simpleName))

    init {
        Runtime.getRuntime().addShutdownHook(
            thread {
                LOGGER.info("Shutdown Hook")
                runBlocking {
                    withLoggedException(LOGGER) {
                        CONNECTIONS.values.forEach {
                            it.disconnect("Closing application")
                            LOGGER.info("Closed connection [${it}]")
                        }

                        SCOPE.cancel()
                        LOGGER.info("Scope cancelled")
                    }
                }
            }
        )
    }

    /**
     * Request a new connection to a given name. Notice that the connection is
     * not connected.
     *
     * @param destinationName the name of the destination
     * @param type the type of the connection (if not specified, it will be used the defult type)
     * @param dns the dns used to resolve the *destinationName* (if not specified it will be used the LocalDNS)
     * @param scope the scope the connection will use (if not specified it will be used the default scope)
     *
     * @return a FunResult containing the opened connection or an error if fails
     */
    suspend fun newConnection(destinationName : String, type : PotConnectionType = PotConnectionType.KTOR, scope : CoroutineScope = SCOPE) : PotConnection {
        val res : PotConnection  = when(type) {
            PotConnectionType.KTOR -> KtorPotConnection(destinationName, scope)
            PotConnectionType.SOCKET_IO -> SocketIOPotConnection(destinationName, scope)
        }

        MUTEX.withLock { CONNECTIONS[destinationName] = res }
        LOGGER.info("Opened connection [$res]")

        return res
    }

    /**
     * Request to disconnect the opened connection to the specified name.
     * If no connection is opened, nothing will be done
     *
     * @param destinationName the name of the end-point of the connection
     * @return an error if something fails or null if successfull
     */
    suspend fun deleteConnection(destinationName: String) : Error? {
        val conn : PotConnection? = MUTEX.withLock {
            CONNECTIONS.remove(destinationName)
        }
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
    suspend fun getConnection(destinationName: String) : Optional<PotConnection> {
        return MUTEX.withLock {
            Optional.ofNullable(CONNECTIONS[destinationName])
        }
    }

    /**
     * Fast operation to send a PotMessage using a previously opened connection.
     * The name of the end-point is taken from the message
     *
     * @param message the message to be sent
     * @return an error if something fails or null if successfull
     */
    suspend fun sendAsync(message : PotMessage) : Error? {
        val conn = MUTEX.withLock { CONNECTIONS[message.destinationName] }
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
        val conn = MUTEX.withLock { CONNECTIONS[name] }
        withNullValue(conn) {
            LOGGER.info("No opened connection for $name")
            return FunResult.fromErrorString("ConnectonManager: no opened connection for $name")
        }

        val listener = SingleMessageListener(conn!!)
        return listener.waitMessage()
    }

}