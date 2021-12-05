package it.greengers.potconnectors.utils

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import org.apache.logging.log4j.kotlin.logger
import java.io.*
import java.net.InetSocketAddress
import java.util.stream.Collectors

fun main(args : Array<String>) {
    val err = Error("error")
    val fr = err.toErrorFunResult<Any>()
    fr.withError { println(it.localizedMessage) }
}