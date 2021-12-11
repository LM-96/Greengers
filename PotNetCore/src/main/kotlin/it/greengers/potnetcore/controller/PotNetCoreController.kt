package it.greengers.potnetcore.controller

import it.greengers.potconnectors.concurrency.newCondition
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
import it.greengers.potnetcore.sensors.polling.*
import it.greengers.potserver.plants.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.kotlin.loggerOf
import java.net.InetSocketAddress
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

object PotNetCoreCoreCoreController {

    @JvmStatic val CONTROLLER_SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this::class.java.name))

    @JvmStatic val SERVER_NAME = "main-server"
    @JvmStatic var SERVER_CONNECTION : PotConnection private set
    @JvmStatic val CONNECTION_MUTEX = Mutex()
    @JvmStatic var RECONNECTOR : Reconnector private set
    @JvmStatic val ON_MESSAGE = this::onMessage

    @JvmStatic val TERMINATE_CHANNEL = Channel<Unit>()

    private val LOGGER = loggerOf(this::class.java)

    init {
        runBlocking {

            //1. Polling Jobs
            startPollingJobs()

            //2. Load DNS for resolving main server name
            loadDNS()

            //3. Enstablish connection to main server
            SERVER_CONNECTION = enstablishConnection()

            //4. Attach reconnector
            RECONNECTOR = attachReconnector()

            //5. Send name to the server
            SERVER_CONNECTION.sendAsyncCommunication(BuiltInCommunicationType.WHOAMI, LocalPotDNS.getApplicationName())

            //6. Manage application shutdown
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
                LOGGER.info("PotNetCore | Perfoming Shutdown Hook")
                runBlocking {
                    SERVER_CONNECTION.removeCallbackOnMessage(ON_MESSAGE)
                    SERVER_CONNECTION.disconnect("Shutdown Hook")
                    PollingJob.POLLING_SCOPE.cancel("Shutdown Hook")
                    CONTROLLER_SCOPE.cancel("Shutdown Hook")
                    TERMINATE_CHANNEL.send(Unit)
                }
                LOGGER.info("PotNetCore | Shutdown Hook completed")
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
        startSensor(TEMPERATURE_SENSOR, "temperature")
        startSensor(HUMIDIY_SENSOR, "humidity")
        startSensor(BRIGHTNESS_SENSOR, "brightness")

    }

    private suspend fun startSensor(sensor : ManagedInputSensor<Double>?, monitoredValue : String) {
        if(sensor!= null) {
            val err = sensor.enableAndStart()
            if(err == null) LOGGER.info("PotNetCore | $monitoredValue monitoring system started")
            else LOGGER.error("PotNetCore | Unable to start $monitoredValue monitoring system: $err")
        } else {
            LOGGER.warn("PotNetCore | Unable to start monitory system on $monitoredValue: no sensor found")
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

    suspend fun <T> sendCriticalValue(sensorType : SensorType, value : T) {
        CONNECTION_MUTEX.withLock {
            SERVER_CONNECTION.sendAsyncValueOutOfRange(sensorType.toString(), value.toString())
        }
    }

    suspend fun send(msg : PotMessage) {
        CONNECTION_MUTEX.withLock {
            SERVER_CONNECTION.sendAsyncMessage(msg)
        }
    }

    suspend fun withSend(producer : () -> PotMessage) {
        send(producer.invoke())
    }

    fun welcome() {
        println(" ________        _____ _____   __      _____     _________                     \n" +
                " ___  __ \\______ __  /____  | / /_____ __  /_    __  ____/______ _____________ \n" +
                " __  /_/ /_  __ \\_  __/__   |/ / _  _ \\_  __/    _  /     _  __ \\__  ___/_  _ \\\n" +
                " _  ____/ / /_/ // /_  _  /|  /  /  __// /_      / /___   / /_/ /_  /    /  __/\n" +
                " /_/      \\____/ \\__/  /_/ |_/   \\___/ \\__/      \\____/   \\____/ /_/     \\___/ ")
    }
}