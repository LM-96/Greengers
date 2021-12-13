package it.greengers.potnetcore

import com.google.gson.Gson
import it.greengers.potconnectors.connection.OkHttpWebsocketConnection
import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potconnectors.messages.buildCommunicationMessage
import it.greengers.potnetcore.controller.Settings
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

fun main(args : Array<String>) {
    runBlocking {
        val wshost = Settings.getSetting("ws-server-host")
        val wsport = Settings.getSetting("ws-server-port")
        LocalPotDNS.registerOrUpdate("ws-heroku-server", InetSocketAddress(wshost, wsport.toInt()))
        val wspath = Settings.getSetting("ws-server-path")
        val conn = OkHttpWebsocketConnection("ws-heroku-server", "")
        println("Connection created")
        conn.connect()
        println(conn.isConnected())
        conn.sendAsyncCommunication("TEMPERATURE", "50")
        conn.disconnect()
    }
}