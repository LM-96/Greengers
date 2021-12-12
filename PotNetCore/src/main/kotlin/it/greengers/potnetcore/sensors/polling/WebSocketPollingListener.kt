package it.greengers.potnetcore.sensors.polling

import it.greengers.potconnectors.messages.buildCommunicationMessage
import it.greengers.potnetcore.controller.WebSocketController
import org.apache.logging.log4j.kotlin.loggerOf

class WebSocketPollingListener<T>(
    job : SensorPollingJob<T>, autoAttach : Boolean = true) : PollingListener<T>(job, autoAttach)
{
    val type = job.sensor.type

    companion object {
        val LOGGER = loggerOf(this::class.java)
    }

    init {
        if(autoAttach)
            attach()
    }

    override suspend fun onPolling(value : T) {
        WebSocketController.safeSend(buildCommunicationMessage("${type.name}", value.toString(), "ws-heroku-server"))
        LOGGER.info("Sent value to websocket")
    }

}