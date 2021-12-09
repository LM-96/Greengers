package it.greengers.potserver.core

import it.greengers.potconnectors.connection.ConnectionManager
import it.greengers.potconnectors.connection.PotConnection
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.*
import it.greengers.potconnectors.utils.Reconnector
import it.greengers.potconnectors.utils.withError
import it.greengers.potserver.plants.PlantState
import it.greengers.potserver.plants.changeCurrentPlantFromJSON
import it.greengers.potserver.plants.withStateToJSON
import it.greengers.potserver.sensors.SensorFactory
import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.net.InetSocketAddress
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
object PotCore {

    @JvmStatic private val SERVER_NAME = "main-server"
    @JvmStatic var SERVER_CONNECTION : PotConnection private set
    @JvmStatic private var RECONNECTOR : Reconnector
    @JvmStatic private var MANAGER_ACTOR = QakContext.getActor("manageractor")!!
    @JvmStatic private val ON_MESSAGE = this::onMessage
    @JvmStatic private val SCOPE = CoroutineScope(EmptyCoroutineContext + CoroutineName(this::javaClass.name))
    @JvmStatic private val SEND_CHANNEL = Channel<PotMessage>()

    init {

        runBlocking {

            //1. Load DNS for resolving main server name
            println("\nPotCore | DNS CONFIGURATION *******************************")
            println("PotCore | Loading DNS...")
            loadDNS()
            println("PotCore | DNS Loaded. Central server address: [${LocalPotDNS.resolve(SERVER_NAME)}")

            //2. Enstablish connection to main server
            println("\nPotCore | CONNECTION TO CENTRAL SERVER ********************")
            val conn = ConnectionManager.newConnection("main-server")
            println("PotCore | Instantiated connection to the central server")
            conn.addCallbackOnMessage(ON_MESSAGE)
            println("PotCore | Message listener attached")
            while(!conn.isConnected()) {
                println("PotCore | Trying to connect...")
                withError(conn.connect()) {
                    println("----> Connection error: ${it.localizedMessage}")
                    delay(2000)
                }
            }
            SERVER_CONNECTION = conn
            println("PotCore | Connected to ${conn.getConnectedAdress()}")

            //3. Attach reconnector
            RECONNECTOR = Reconnector.attachPersistentReconnector(conn)
            println("PotCore | Attached automatic reconnector")

            //4. Send name to the server
            SEND_CHANNEL.send(buildCommunicationMessage("whoami", LocalPotDNS.getApplicationName(), SERVER_NAME))

            //5. Manage application shutdown
            Runtime.getRuntime().addShutdownHook(
                thread {
                    SEND_CHANNEL.close()
                    SCOPE.cancel()
                    runBlocking {
                        SERVER_CONNECTION.removeCallbackOnMessage(ON_MESSAGE)
                        SERVER_CONNECTION.disconnect("application shutdown")
                    }
                }
            )
        }
    }

    private suspend fun loadDNS() {
        LocalPotDNS.resolve(SERVER_NAME)
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
                val actorName = applMessage.msgReceiver()
                val actor = QakContext.getActor(actorName)
                if(actor == null) {
                    println("----> Actor $actorName does not exist")
                    val msg = buildErrorMessage("Actor $actorName does not exist", message.destinationName)
                    send(msg)
                } else {
                    println("----> Redirecting application message to the actor $actorName")
                    MsgUtil.sendMsg(applMessage, actor!!)
                }
            }

            PotMessageType.STATE_REQUEST -> {
                println("----> Redirecting state request to $MANAGER_ACTOR")
                val applMessage = MsgUtil.buildDispatch("potcore", "stateRequest", message.senderName, MANAGER_ACTOR.name)
                MsgUtil.sendMsg(applMessage, MANAGER_ACTOR)
            }

            else -> {
                println("----> Operation not already supported")
                val msg = buildUnsupportedOperationMessage("Not already supported", message, message.destinationName)
                send(msg)
            }
        }
    }

    private fun writerListener() {
        SCOPE.launch {
                while (true) {
                    try {
                        val msg = SEND_CHANNEL.receive()
                        var err = SERVER_CONNECTION.sendAsyncMessage(msg)
                        while(err != null){
                            println("PotCore [writer] | $err\n----> Waiting connection")
                            RECONNECTOR.waitConnection()
                            err = SERVER_CONNECTION.sendAsyncMessage(msg)
                        }
                    } catch (e : Exception) {
                        e.printStackTrace()
                        continue
                    }
                }
        }
    }

    suspend fun send(message : PotMessage) {
        SEND_CHANNEL.send(message)
    }

    suspend fun sendState(destinationName : String, state : PlantState = CurrentPlant.STATE) {
        SEND_CHANNEL.send(buildStateReplyMessage(state.temperature, state.brightness, state.humidity, -1.0, destinationName))
    }

    suspend fun sendValueOutOfRange(sensorId : String, currValue : String) {
        val valueType = SensorFactory.getSensorType(sensorId)
        if(valueType == null) {
            println("PotCore | Unable to find a type for ValueOutRange message [sensorId=$sensorId, currValue=$currValue]")
            return
        }

        SEND_CHANNEL.send(buildValueOutOfRangeMessage(valueType.toString(), currValue, SERVER_NAME))
    }

}