package it.greengers.potnetcore.sensors

class ValueRange<T> (
    val min : T,
    val max : T) where T : Comparable<T> {

    fun isInRange(value : T) : Boolean {
        return value in min..max
    }

    fun isOutOfRange(value : T) : Boolean {
        return value !in min..max
    }
}