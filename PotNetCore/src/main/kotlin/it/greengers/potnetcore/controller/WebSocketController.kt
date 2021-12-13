package it.greengers.potnetcore.controller

import it.greengers.potconnectors.connection.ConnectionManager
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.connection.PotConnectionType
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.withError
import it.greengers.potnetcore.sensors.polling.*
import it.greengers.potserver.plants.changeCurrentPlantFromJSON
import it.greengers.potserver.plants.withStateToJSON
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.kotlin.loggerOf
import java.net.InetSocketAddress

object WebSocketController {

    var WEBSOCKET_CONNECTION : PotConnection? = null
        private set
    val WEBSOCKET_MUTEX = Mutex()
    var WEBSOCKET_JOB : Job? = null
        private set
    val CLOSE = Channel<Unit>()

    val LOGGER = loggerOf(this::class.java)
    val ON_MESSAGE = this::onMessage

    fun welcome() {
        println(" ________        _____ _____   __      _____     _________                     \n" +
                " ___  __ \\______ __  /____  | / /_____ __  /_    __  ____/______ _____________ \n" +
                " __  /_/ /_  __ \\_  __/__   |/ / _  _ \\_  __/    _  /     _  __ \\__  ___/_  _ \\\n" +
                " _  ____/ / /_/ // /_  _  /|  /  /  __// /_      / /___   / /_/ /_  /    /  __/\n" +
                " /_/      \\____/ \\__/  /_/ |_/   \\___/ \\__/      \\____/   \\____/ /_/     \\___/\n" +
                "                                     ** WEBSOCKET ONLY ** "
        )
    }

    fun startAsync(scope : CoroutineScope = GlobalScope) {
        WEBSOCKET_JOB = scope.launch {
            startPollingJobs()
            LOGGER.info("PotNetCoreWS | Attempting to connect...")
            val wshost = Settings.getSetting("ws-server-host")
            val wsport = Settings.getSetting("ws-server-port")
            LocalPotDNS.registerOrUpdate("ws-heroku-server", InetSocketAddress(wshost, wsport.toInt()))
            val wspath = Settings.getSetting("ws-server-path")
            LOGGER.info("PotNetCoreWS | Requesting connection to ConnectionManager")
            val conn = ConnectionManager.newConnection("ws-heroku-server", PotConnectionType.WEBSOCKET,
                scope, wspath)
            LOGGER.info("PotNetCoreWS | Obtained connection $conn")
            conn.addCallbackOnMessage(ON_MESSAGE)
            LOGGER.info("PotNetCoreWS | Message listener attached")
            while(!conn.isConnected()) {
                LOGGER.info("PotNetCoreWS| Trying to connect...")
                withError(conn.connect()) {
                    LOGGER.error("----> Connection error: ${it.localizedMessage}")
                    delay(2000)
                }
            }
            WEBSOCKET_CONNECTION = conn
            LOGGER.info("PotCore | Connected to ${conn.getConnectedAdress()}")
        }
    }

    private suspend fun startPollingJobs() {
        startSensor(TEMPERATURE_SENSOR, "temperature")
        startSensor(HUMIDIY_SENSOR, "humidity")
        startSensor(BRIGHTNESS_SENSOR, "brightness")

    }

    private suspend fun startSensor(sensor : ManagedInputSensor<Double>?, monitoredValue : String) {
        sensor?.addListener {
            WebSocketPollingListener(it) }
        if(sensor!= null) {
            val err = sensor.enableAndStart()
            if(err == null) LOGGER.info("PotNetCore | $monitoredValue monitoring system started")
            else LOGGER.error("PotNetCore | Unable to start $monitoredValue monitoring system: $err")
        } else {
            LOGGER.warn("PotNetCore | Unable to start monitory system on $monitoredValue: no sensor found")
        }
    }

    private suspend fun onMessage(message: PotMessage) {
        WEBSOCKET_MUTEX.withLock {
            LOGGER.info("PotNetCoreWs | Received message [$message]")
            when(message.type) {
                PotMessageType.COMMUNICATION -> handleCommunicationMessage(message as CommunicationMessage)
                PotMessageType.STATE_REQUEST -> handleStateRequest(message as StateRequestMessage)

                else -> {
                    LOGGER.error("----> Operation not already supported")
                    val msg = buildUnsupportedOperationMessage("Not already supported", message, message.senderName)
                    unsafeSend(msg)
                }
            }
        }
    }

    private suspend fun unsafeSend(msg : PotMessage) {
        WEBSOCKET_CONNECTION?.sendAsyncMessage(msg) ?: LOGGER.error("Unable to send message [$msg]: job not started")
    }

    suspend fun safeSend(msg: PotMessage) {
        WEBSOCKET_MUTEX.withLock {
            if(WEBSOCKET_CONNECTION == null) LOGGER.error("Unable to send message [$msg]: job not started")
            else {
                WEBSOCKET_CONNECTION!!.sendAsyncMessage(msg)
                LOGGER.info("Sent message $msg")
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
                        LOGGER.info("PotNetCoreWs | Current plant updated [${CurrentPlant.CURRENT_PLANT}")
                    } catch (e : Exception) {
                        LOGGER.error(e)
                        unsafeSend(buildErrorMessage(e, message.senderName))
                    }
                }
                BuiltInCommunicationType.REQUEST_COMPLETE_STATE -> {
                    try {
                        unsafeSend(
                            buildCommunicationMessage(BuiltInCommunicationType.COMPLETE_STATE, CurrentPlant.withStateToJSON(), message.senderName)
                        )
                    } catch (e : Exception) {
                        LOGGER.error(e)
                        unsafeSend(buildErrorMessage(e, message.senderName))
                    }
                }
            }
        }
    }

    private suspend fun handleStateRequest(message: StateRequestMessage) {
        unsafeSend(
            buildStateReplyMessage(
            CurrentPlant.STATE.temperature,
            CurrentPlant.STATE.humidity,
            CurrentPlant.STATE.brightness,
            CurrentPlant.STATE.battery,
            message.senderName
        )
        )
        LOGGER.info("PotNetCoreWs | Sent state to [${message.senderName}]")
    }

    fun await() {
        runBlocking {
            CLOSE.receive()
        }
    }

    fun close() {
        runBlocking {
            CLOSE.send(Unit)
        }
    }
}