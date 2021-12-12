package it.greengers.potnetalexa.speeching

import kotlin.random.Random

fun <T> List<T>.getCasual() : T {
    return get(Random.nextInt(size))
}

enum class Language {
    ITALIAN
}