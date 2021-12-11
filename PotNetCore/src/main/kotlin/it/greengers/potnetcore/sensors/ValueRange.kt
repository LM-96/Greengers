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

    fun ifOutOfRange(value : T, action : (T) -> Unit) : ValueRange<T> {
        if(value !in min..max)
            action.invoke(value)

        return this
    }

    fun ifInRange(value : T, action : (T) -> Unit) : ValueRange<T> {
        if(value in min..max)
            action.invoke(value)

        return this
    }

    override fun toString(): String {
        return "ValueRange[min=$min, max=$max]"
    }


}