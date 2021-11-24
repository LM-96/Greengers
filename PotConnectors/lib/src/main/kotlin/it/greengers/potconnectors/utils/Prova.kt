package it.greengers.potconnectors.utils

import org.apache.logging.log4j.kotlin.logger

fun main(args : Array<String>) {
    val err = withExceptionToError(logger("main")) {
        val op = 5 / 0
    }
}