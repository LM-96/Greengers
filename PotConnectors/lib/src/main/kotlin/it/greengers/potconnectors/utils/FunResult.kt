package it.greengers.potconnectors.utils

import kotlin.reflect.KClass

class FunResult<T> private constructor(
    val res : T?,
    val error : Error?
) {

    constructor(value : T) : this(value, null)
    constructor(error : Error) : this(null, error)
    constructor(throwable : Throwable) : this(null, Error(throwable))

    companion object {
        @JvmStatic
        inline fun <T, reified K> castErrorFunResult(orig : FunResult<T>) : FunResult<K> {
            return orig.castWithError()
        }
    }

    fun thereIsError() : Boolean {
        return error != null
    }

    fun thereIsValue() : Boolean {
        return res != null
    }

    fun handleError(handler : (Error) -> Unit) : FunResult<T> {
        if(error != null)
            handler.invoke(error)

        return this
    }

    fun handleResult(handler : (T) -> Unit) : FunResult<T> {
        if(res != null)
            handler.invoke(res)

        return this
    }

    fun <K> castWithError() : FunResult<K> {
        return if(thereIsError()) {
            FunResult(error!!)
        } else {
            FunResult(Error("Cannot cast because value is present in [${toString()}]"))
        }
    }

    override fun toString(): String {
        return if(error != null)
            "Error[$error]"
        else
            "Value[$res]"
    }

    inline fun withError(then : (e : Error) -> Unit) : FunResult<T> {
        if(error != null) then.invoke(error!!)
        return this
    }

    inline fun withValue(then : (value : T) -> Unit) : FunResult<T> {
        if(res != null) then.invoke(res!!)
        return this
    }

    inline fun withThisIfError(then : (e : FunResult<T>) -> Unit) : FunResult<T> {
        if(error != null) then.invoke(this)
        return this
    }

    inline fun withThisIfValue(then : (e : FunResult<T>) -> Unit) : FunResult<T> {
        if(res != null) then.invoke(this)
        return this
    }
}