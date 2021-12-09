package it.greengers.potconnectors.utils

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import it.greengers.potconnectors.connection.identificateAndThen
import java.net.InetSocketAddress

fun main(args : Array<String>) {
    runBlocking {
        val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(InetSocketAddress("127.0.0.1", 2323))
        socket.identificateAndThen{

        }
    }
}