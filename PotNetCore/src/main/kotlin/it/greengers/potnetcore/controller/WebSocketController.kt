package it.greengers.potnetcore.controller

import it.greengers.potconnectors.connection.ConnectionManager
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.connection.PotConnectionType
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.withError
import it.greengers.potnetcore.sensors.polling.TEMPERATURE_SENSOR
import it.greengers.potnetcore.sensors.polling.WebSocketPollingListener
import it.greengers.potserver.plants.changeCurrentPlantFromJSON
import it.greengers.potserver.plants.withStateToJSON
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val LOGGER = loggerOf(this::class.java)

    fun startAsync() {
        WEBSOCKET_JOB = PotNetCoreCoreCoreController.CONTROLLER_SCOPE.launch {
            val wshost = Settings.getSetting("ws-server-host")
            val wsport = Settings.getSetting("ws-server-port")
            LocalPotDNS.registerOrUpdate("ws-heroku-server", InetSocketAddress(wshost, wsport.toInt()))
            val wspath = Settings.getSetting("ws-server-path")
            val conn = ConnectionManager.newConnection("ws-heroku-server", PotConnectionType.WEBSOCKET,
                PotNetCoreCoreCoreController.CONTROLLER_SCOPE, wspath)
            conn.addCallbackOnMessage(PotNetCoreCoreCoreController.ON_MESSAGE)
            LOGGER.info("PotCore | Message listener attached")
            while(!conn.isConnected()) {
                LOGGER.info("PotCore | Trying to connect...")
                withError(conn.connect()) {
                    LOGGER.error("----> Connection error: ${it.localizedMessage}")
                    delay(2000)
                }
            }
            WEBSOCKET_CONNECTION = conn
            LOGGER.info("PotCore | Connected to ${conn.getConnectedAdress()}")
            TEMPERATURE_SENSOR?.addListener {
                WebSocketPollingListener(it) }
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
            WEBSOCKET_CONNECTION?.sendAsyncMessage(msg) ?: LOGGER.error("Unable to send message [$msg]: job not started")
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


}