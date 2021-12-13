package it.greengers.potnetcore

import it.greengers.potconnectors.dns.LocalPotDNS
import it.greengers.potnetcore.controller.WebSocketController

fun main(args : Array<String>) {
    LocalPotDNS.setApplicationName("potnet_1234567")
    WebSocketController.welcome()
    WebSocketController.startAsync()
    WebSocketController.await()
}