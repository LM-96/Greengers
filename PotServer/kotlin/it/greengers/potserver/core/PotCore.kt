package it.greengers.potserver.core

import it.greengers.potconnectors.connection.ConnectionManager
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.ActorMessage
import it.greengers.potconnectors.messages.PotMessage
import it.greengers.potconnectors.messages.PotMessageType
import it.greengers.potconnectors.utils.Reconnector
import it.greengers.potconnectors.utils.withNotNullValue
import it.unibo.kactor.ActorBasicFsm
import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.net.InetSocketAddress
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
object PotCore {

    @JvmStatic private var SERVER_CONNECTION : PotConnection
    @JvmStatic private var RECONNECTOR : Reconnector
    @JvmStatic private var MANAGER_ACTOR = QakContext.getActor("manageractor")!!
    @JvmStatic private val ON_MESSAGE = this::onMessage
    @JvmStatic private val SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this::javaClass.name))
    @JvmStatic private val CHANNEL = Channel<PotMessage>()

    init {

        runBlocking {
            //1. Load DNS for resolving main server name
            println("\nPotCore | DNS CONFIGURATION *******************************")
            println("PotCore | Loading DNS...")
            loadDNS()
            println("PotCore | DNS Loaded. Central server address: [${LocalPotDNS.resolve("main-server")}")

            //2. Enstablish connection to main server
            println("\nPotCore | CONNECTION TO CENTRAL SERVER ********************")
            val conn = ConnectionManager.newConnection("main-server")
            println("PotCore | Instantiated connection to the central server")
            conn.addCallbackOnMessage(ON_MESSAGE)
            println("PotCore | Message listener attached")
            while(!conn.isConnected()) {
                println("PotCore | Trying to connect...")
                conn.connect()
                delay(2000)
            }
            SERVER_CONNECTION = conn
            println("PotCore | Connected to ${conn.getConnectedAdress()}")

            //3. Attach reconnector
            RECONNECTOR = Reconnector.attachPersistentReconnector(conn)
            println("PotCore | Attached automatic reconnector")

        }
    }

    private suspend fun loadDNS() {
        LocalPotDNS.resolve("main-server")
            .withError {
                println("PotCore | Central Server address is not into DNS. Will be used the default configuration")
                val addr = Settings.getSetting("main-server-address")
                val port = Settings.getSetting("main-server-port").toInt()
                LocalPotDNS.registerOrUpdate("main-server", InetSocketAddress(addr, port))
                println("PotCore | Added default central server address and port to the DNS")
                LocalPotDNS.makeBackup()
                println("PotCore | DNS backup successfully done")
            }
            .withValue {
                println("PotCore | Found central server address: $it")
            }
    }

    private suspend fun onMessage(message : PotMessage) {
        println("PotCore | Received message [$message]")
        when(message.type) {
            PotMessageType.ACTOR -> {
                val applMessage = (message as ActorMessage).applMessage
                val actor = QakContext.getActor(applMessage.msgReceiver())
                println("----> Redirecting application message to the actor ${actor?.name}")
                MsgUtil.sendMsg(applMessage, actor!!)
            }

            PotMessageType.STATE_REQUEST -> {
                println("----> Redirecting state request to $MANAGER_ACTOR")
                val applMessage = MsgUtil.buildDispatch("potcore", "stateRequest", "STATE", MANAGER_ACTOR.name)
                MsgUtil.sendMsg(applMessage, MANAGER_ACTOR)
            }
        }
    }

    private fun writerListener() {
        SCOPE.launch {
                while (!CHANNEL.isClosedForSend) {
                    try {
                        val msg = CHANNEL.receive()
                        val err = SERVER_CONNECTION.sendAsyncMessage(msg)

                    } catch (e : Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    suspend fun send(message : PotMessage) {
        CHANNEL.send(message)
    }

}