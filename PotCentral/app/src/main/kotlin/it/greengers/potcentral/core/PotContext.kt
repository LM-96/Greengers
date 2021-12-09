package it.greengers.potcentral.core

import it.greengers.potcentral.handlers.HandledPotConnection

open class PotContext(
    val potId : String,
    var potConnection : HandledPotConnection? = null,
    val clientConnections: MutableList<HandledPotConnection> = mutableListOf()
) {

    override fun toString(): String {
        return "PotContext(potId='$potId', potConnection=$potConnection, clientConnections=$clientConnections)"
    }
}