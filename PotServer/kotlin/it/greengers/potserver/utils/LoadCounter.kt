package it.greengers.potserver.utils

import java.util.function.Function
import java.util.stream.Collectors
import kotlin.jvm.Throws

class LoadCounter(
    elements: Collection<Any>
) {

    private val elements = elements.associateBy({it}, {false}).toMutableMap()

    fun setLoaded(element : Any) : Boolean {
        if(!elements.containsKey(element))
            return false

        elements[element] = true
        return true
    }

    fun setUnloaded(element: Any) : Boolean {
        if(!elements.containsKey(element))
            return false

        elements[element] = false
        return true
    }

    fun isLoaded(element : Any) : Boolean? {
        return elements[element]
    }

    fun allLoaded() : Boolean {
        for(loaded in elements.values)
            if(!loaded)
                return false

        return true
    }

    fun unloadedElements() : Int {
        return elements.filter { !it.value }.count()
    }

    fun loadedElements() : Int {
        return elements.filter { it.value }.count()
    }

    fun totalElements() : Int {
        return elements.size
    }

}