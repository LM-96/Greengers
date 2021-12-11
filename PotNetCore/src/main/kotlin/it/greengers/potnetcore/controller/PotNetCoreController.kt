package it.greengers.potnetcore.controller

import it.greengers.potconnectors.connection.ConnectionManager
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.Reconnector
import it.greengers.potconnectors.utils.withError
import it.greengers.potnetcore.sensors.HumiditySensor
import it.greengers.potnetcore.sensors.InputSensor
import it.greengers.potnetcore.sensors.SensorFactory
import it.greengers.potnetcore.sensors.SensorType
import it.greengers.potnetcore.sensors.polling.PollingJob
import it.greengers.potnetcore.sensors.polling.PollingJobState
import it.greengers.potnetcore.sensors.polling.TEMPERATURE_JOB
import it.greengers.potnetcore.sensors.polling.newPollingJob
import it.greengers.potserver.plants.*
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.loggerOf
import java.net.InetSocketAddress
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

object PotNetCoreCoreCoreController {

    @JvmStatic val CONTROLLER_SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this::class.java.name))

    @JvmStatic val SERVER_NAME = "main-server"
    @JvmStatic var SERVER_CONNECTION : PotConnection private set
    @JvmStatic var RECONNECTOR : Reconnector private set
    @JvmStatic val ON_MESSAGE = this::onMessage

    private val LOGGER = loggerOf(this::class.java)

    init {
        runBlocking {

            //1. Load DNS for resolving main server name
            loadDNS()

            //2. Enstablish connection to main server
            SERVER_CONNECTION = enstablishConnection()

            //3. Attach reconnector
            RECONNECTOR = attachReconnector()

            //4. Launch writer and send name to the server
            SERVER_CONNECTION.sendAsyncCommunication(BuiltInCommunicationType.WHOAMI, LocalPotDNS.getApplicationName())

            //5. Polling Jobs
            startPollingJobs()

            //5. Manage application shutdown
            addShutdownHook()
        }
    }

    private suspend fun enstablishConnection() : PotConnection {
        LOGGER.info("\nPotCore | CONNECTION TO CENTRAL SERVER ********************")
        val conn = ConnectionManager.newConnection(destinationName = "main-server", scope = CONTROLLER_SCOPE)
        LOGGER.info("PotCore | Instantiated connection to the central server")
        conn.addCallbackOnMessage(ON_MESSAGE)
        LOGGER.info("PotCore | Message listener attached")
        while(!conn.isConnected()) {
            LOGGER.info("PotCore | Trying to connect...")
            withError(conn.connect()) {
                LOGGER.error("----> Connection error: ${it.localizedMessage}")
                delay(2000)
            }
        }
        LOGGER.info("PotCore | Connected to ${SERVER_CONNECTION.getConnectedAdress()}")
        return conn
    }

    private suspend fun attachReconnector() : Reconnector {
        val reconnector = Reconnector.attachPersistentReconnector(SERVER_CONNECTION)
        LOGGER.info("PotCore | Attached automatic reconnector")

        return reconnector
    }

    private suspend fun addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            thread {
                LOGGER.info("Perfoming Shutdown Hook")
                runBlocking {
                    SERVER_CONNECTION.removeCallbackOnMessage(ON_MESSAGE)
                    SERVER_CONNECTION.disconnect("Shutdown Hook")
                    CONTROLLER_SCOPE.cancel("Shutdown Hook")
                }
                LOGGER.info("Shutdown Hook completed")
            }
        )
    }

    private suspend fun loadDNS() {
        LOGGER.info("\nPotNetCore | DNS CONFIGURATION *******************************")
        LOGGER.info("PotNetCore | Loading DNS...")
        LocalPotDNS.resolve(SERVER_NAME)
            .withError {
                LOGGER.info("PotNetCore | Central Server address is not into DNS. Will be used the default configuration")
                val addr = Settings.getSetting("main-server-address")
                val port = Settings.getSetting("main-server-port").toInt()
                LocalPotDNS.registerOrUpdate("main-server", InetSocketAddress(addr, port))
                LocalPotDNS.setApplicationName("pot-0")
                LOGGER.info("PotNetCore | Added default central server address and port to the DNS")
                LocalPotDNS.makeBackup()
                LOGGER.info("PotNetCore | DNS backup successfully done")
            }
            .withValue {
                LOGGER.info("PotNetCore | Found central server address: $it")
            }
        LOGGER.info("PotNetCore | DNS Loaded. Central server address: [${LocalPotDNS.resolve(SERVER_NAME)}")
    }

    private suspend fun startPollingJobs() {
        if(TEMPERATURE_JOB != null) {
            TEMPERATURE_JOB.start()
            LOGGER.info("PotNetCore | Temperature polling started")
        } else {
            LOGGER.warn("PotNetCore | Unable to start polling on temperaturr")
        }
    }


    private suspend fun onMessage(message: PotMessage) {
        LOGGER.info("PotNetCore | Received message [$message]")
        when(message.type) {
            PotMessageType.COMMUNICATION -> handleCommunicationMessage(message as CommunicationMessage)
            PotMessageType.STATE_REQUEST -> handleStateRequest(message as StateRequestMessage)

            else -> {
                LOGGER.error("----> Operation not already supported")
                val msg = buildUnsupportedOperationMessage("Not already supported", message, message.senderName)
                SERVER_CONNECTION.sendAsyncMessage(msg)
            }
        }
    }

    private suspend fun handleCommunicationMessage(message : CommunicationMessage) {
        val type = message.isBuiltInCommunicationType()
        if(type.isPresent) {
            when(type.get()) {
                BuiltInCommunicationType.PLANTCHANGE -> {
                    try {
                        CurrentPlant.changeCurrentPlantFromJSON(message.communication)
                        LOGGER.info("PotNetCoreCore | Current plant updated [${CurrentPlant.CURRENT_PLANT}")
                    } catch (e : Exception) {
                        LOGGER.error(e)
                        SERVER_CONNECTION.sendAsyncMessage(buildErrorMessage(e, message.senderName))
                    }
                }
                BuiltInCommunicationType.REQUEST_COMPLETE_STATE -> {
                    try {
                        SERVER_CONNECTION.sendAsyncMessage(
                            buildCommunicationMessage(BuiltInCommunicationType.COMPLETE_STATE, CurrentPlant.withStateToJSON(), message.senderName)
                        )
                    } catch (e : Exception) {
                        LOGGER.error(e)
                        SERVER_CONNECTION.sendAsyncMessage(buildErrorMessage(e, message.senderName))
                    }
                }
            }
        }
    }

    private suspend fun handleStateRequest(message: StateRequestMessage) {
        SERVER_CONNECTION.sendAsyncMessage(buildStateReplyMessage(
            CurrentPlant.STATE.temperature,
            CurrentPlant.STATE.humidity,
            CurrentPlant.STATE.brightness,
            CurrentPlant.STATE.battery,
            message.senderName
        ))
        LOGGER.info("PotNetCore | Sent state to [${message.senderName}]")
    }

    fun criticalValue
}