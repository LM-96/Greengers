package it.greengers.potconnectors.utils

import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.*
import it.unibo.kactor.ApplMessage
import kotlinx.coroutines.channels.Channel
import org.apache.logging.log4j.kotlin.loggerOf

class StateRequestUtil (
    private val potConnection: PotConnection
){

    companion object {
        @JvmStatic val LOGGER = loggerOf(this::class.java)
    }

    private val chan = Channel<PotMessage>()
    private val onMsg4StateReq = this::onMessageForStateRequest
    private val onMsg4Actor = this::onMessageForActor

    suspend fun attachForSingleRequest()  : StateRequestUtil {
        potConnection.addCallbackOnMessage(onMsg4StateReq)
        LOGGER.info("Waiting for state reply from [${potConnection.getConnectedAdress()}]")
        return this
    }

    suspend fun attachForActorReques() : StateRequestUtil {
        potConnection.addCallbackOnMessage(onMsg4Actor)
        LOGGER.info("Waiting for actor reply from [${potConnection.getConnectedAdress()}]")
        return this
    }

    suspend fun onMessageForStateRequest(potMessage: PotMessage) {
        if(potMessage.type == PotMessageType.STATE_REPLY) {
            chan.send(potMessage)
            potConnection.removeCallbackOnMessage(onMsg4StateReq)
            LOGGER.info("Found state reply from [${potConnection.getConnectedAdress()}]. Detached callback...")
        }
    }

    suspend fun onMessageForActor(potMessage: PotMessage) {
        if(potMessage.type == PotMessageType.ACTOR) {
            try {
                val msg = (potMessage as ActorMessage).toQakMessage()
                if(msg.isReply()) {
                    chan.send(potMessage)
                    potConnection.removeCallbackOnMessage(onMsg4Actor)
                    LOGGER.info("Found actor reply from [${potConnection.getConnectedAdress()}]. Detached callback...")
                }
            } catch (e : Exception) {
                chan.send(ErrorMessage("", "", e.localizedMessage))
                potConnection.removeCallbackOnMessage(onMsg4Actor)

                LOGGER.error("Error waiting actor reply. Callback is been however detached.")
            }
        }
    }

    suspend fun performStateRequestAndWaitResponse() : FunResult<StateReplyMessage> {

        LOGGER.info("Performing state request to [${potConnection.getConnectedAdress()}]...")
        val err = potConnection.sendAsyncStateRequest()
        if(err != null) return FunResult(error = err)

        val res = try {
            val received = chan.receive()
            LOGGER.info("Received response message from [${potConnection.getConnectedAdress()}]")
            FunResult(res = received as StateReplyMessage)
        } catch(e : Exception) {
            LOGGER.error(e)
            FunResult(error = Error(e))
        }
        chan.close()

        return res
    }

    suspend fun performActorRequest(msg : String) : FunResult<ActorMessage> {

        LOGGER.info("Performing actor request [$msg] to [${potConnection.getConnectedAdress()}]...")
        val applMsg = try {
            ApplMessage(msg)
        } catch (e : Exception) {
            LOGGER.error(e)
            return FunResult(error = Error(e))
        }

        if(!applMsg.isRequest()) {
            LOGGER.warn("The message [$msg] is not an actor request")
            return FunResult(error = Error("The message [$msg] is not an actor request"))
        }

        val err = potConnection.sendAsyncRawActorMessage(msg)
        if(err != null) return FunResult(error = err)

        val res = try {
            val received = chan.receive()
            LOGGER.info("Received response message from [${potConnection.getConnectedAdress()}]")
            FunResult(received as ActorMessage)
        } catch (e : Exception) {
            LOGGER.error(e)
            FunResult(error = Error(e))
        }
        chan.close()

        return res
    }

}