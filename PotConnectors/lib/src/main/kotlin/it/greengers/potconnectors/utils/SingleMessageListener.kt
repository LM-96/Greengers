package it.greengers.potconnectors.utils

import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.messages.PotMessage
import kotlinx.coroutines.channels.Channel

class SingleMessageListener(private val connection: PotConnection) {

    private var channel = Channel<PotMessage>()

    suspend fun waitMessage() : FunResult<PotMessage> {
        val callback = this::onMsg
        connection.addCallbackOnMessage(callback)
        val res = withExceptionToFunResult {
            channel.receive()
        }
        channel.close()
        connection.removeCallbackOnMessage(callback)

        return res
    }

    private suspend fun onMsg(msg : PotMessage) {
        channel.send(msg)
        channel.close()
    }

}