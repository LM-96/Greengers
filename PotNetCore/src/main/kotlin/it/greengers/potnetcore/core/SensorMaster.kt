package it.greengers.potnetcore.core

import it.greengers.potnetcore.sensors.Sensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger
import kotlin.concurrent.thread

abstract class SensorMaster(
    val sensor : Sensor,
    private val scope : CoroutineScope,
    private val ctxFlow : SharedFlow<MasterMessage>,
    private val input : Channel<MasterMessage> = Channel(),
    private val output : Channel<MasterMessage> = Channel(),
) {

    protected val channelListener : Job
    protected val flowListener : Job
    protected val logger = logger("SensorMaster[${sensor.id}]")

    init {
        channelListener = scope.launch {
            while(!input.isClosedForReceive) {
                try {
                    onMessage(input.receive())
                } catch (e : Exception) {
                    logger.error(e)
                    continue
                }
            }
        }

        flowListener = scope.launch {
            ctxFlow.collect{
                onMessage(it)
            }
        }

        Runtime.getRuntime().addShutdownHook(
            thread {
                input.close()
                output.close()
                channelListener.cancel("Shutdown Hook")
                flowListener.cancel("Shutdown Hook")
            }
        )
    }

    protected abstract suspend fun onRequest(msg : MasterMessage)
    protected abstract suspend fun onReply(msg : MasterMessage)
    protected abstract fun onDispatch(msg : MasterMessage)
    protected abstract fun onEvent(msg : MasterMessage)

    private suspend fun onMessage(msg : MasterMessage) {
        logger.info("Received message: [$msg]")
        when(msg.messageType) {
            MasterMessageType.DISPATCH -> onDispatch(msg)
            MasterMessageType.EVENT -> onEvent(msg)
            MasterMessageType.REQUEST -> onRequest(msg)
            MasterMessageType.REPLY -> onReply(msg)
        }
    }

    fun getInput() : ReceiveChannel<MasterMessage> {
        return output
    }

    fun getOutput() : SendChannel<MasterMessage> {
        return input
    }

}