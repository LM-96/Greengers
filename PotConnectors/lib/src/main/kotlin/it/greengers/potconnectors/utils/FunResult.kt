package it.greengers.potconnectors.utils

import kotlin.reflect.KClass

/**
 * A class representig the *result* of a function that can also produce an error.
 * An object of this class contains the value if correctly produced, or an error
 * if the function fails.
 */
class FunResult<T> private constructor(
    val res : T?,
    val error : Error?
) {

    constructor(value : T) : this(value, null)
    constructor(error : Error) : this(null, error)
    constructor(throwable : Throwable) : this(null, Error(throwable))

    companion object {
        /**
         * Cast a FunResult object that contains an error
         *
         * @param the FunResult object to cast
         * @param T the type of the value of the original FunResult
         * @param K the desired type
         * @return a casted FunResult object
         */
        @JvmStatic
        inline fun <T, reified K> castErrorFunResult(orig : FunResult<T>) : FunResult<K> {
            return orig.castWithError()
        }

        /**
         * Generate a FunResult object that contains an error with the message
         * passed as a parameter
         *
         * @param the error message
         * @return a FunResult object that contains an error with the given message
         */
        @JvmStatic fun <T> fromErrorString(string: String) : FunResult<T> {
            return FunResult(Error(string))
        }
    }

    /**
     * Return true if this FunResult object contains an error
     * @return true if this FunResult object contains an error or false
     * if contains a value
     */
    fun thereIsError() : Boolean {
        return error != null
    }

    /**
     * Return true if this FunResult object contains a value
     * @return true if this FunResult object contains a value or false
     * if containts an error
     */
    fun thereIsValue() : Boolean {
        return res != null
    }

    /**
     * Invoke the given function passing the error contained in this
     * FunResult object.
     * If this FunResult object does not contain an error, the function
     * will not be invoked
     *
     * @param handler the function to be invoked with the non-null error
     * @return this object
     */
    fun handleError(handler : (Error) -> Unit) : FunResult<T> {
        if(error != null)
            handler.invoke(error)

        return this
    }

    /**
     * Invoke the given function passing the value contained in this
     * FunResult object.
     * If this FunResult object does not contain a value, the function
     * will not be invoked
     *
     * @param handler the function to be invoked with the non-null value
     * @return this object
     */
    fun handleResult(handler : (T) -> Unit) : FunResult<T> {
        if(res != null)
            handler.invoke(res)

        return this
    }

    /**
     * Cast this object to another value type only if it contains
     * an error. If this FunResult contains a value, then a new FunResult
     * is returned with an error
     *
     * @return the casted FunResult object or a new with an error if this
     * object contains a value
     */
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

    /**
     * Perform the given action passing it the error contained in this
     * FunResult object.
     * If this FunResult object does not contain an error, the action
     * will not be performed
     *
     * @param handler the action to be invoked with the non-null error
     * @return this object
     */
    inline fun withError(then : (e : Error) -> Unit) : FunResult<T> {
        if(error != null) then.invoke(error)
        return this
    }

    /**
     * Perform the given action passing it the value contained in this
     * FunResult object.
     * If this FunResult object does not contain a value, the action
     * will not be performed
     *
     * @param handler the action to be performed with the non-null value
     * @return this object
     */
    inline fun withValue(then : (value : T) -> Unit) : FunResult<T> {
        if(res != null) then.invoke(res)
        return this
    }

    /**
     * Perform the given action passing it this FunResult object that contains an error.
     * If this FunResult object does not contain an error, the action
     * will not be performed
     *
     * @param handler the action to be performed with the non-null error
     * @return this object
     */
    inline fun withThisIfError(then : (e : FunResult<T>) -> Unit) : FunResult<T> {
        if(error != null) then.invoke(this)
        return this
    }

    /**
     * Perform the given action passing it this FunResult object that contains a value.
     * If this FunResult object does not contain a value, the action
     * will not be performed
     *
     * @param handler the action to be performed with the non-null value
     * @return this object
     */
    inline fun withThisIfValue(then : (e : FunResult<T>) -> Unit) : FunResult<T> {
        if(res != null) then.invoke(this)
        return this
    }
}

/**
 * Cast this to a FunResult object that will contain this as a value
 *
 * @return this as a FunResult object
 */
inline fun <reified T> Any.toFunResult() : FunResult<T>{
    return FunResult(this as T)
}

/**
 * Cast this error to a FunResult object that will contain this as en error
 *
 * @return this as a FunResult object with this error
 */
inline fun <reified T> Error.toErrorFunResult() : FunResult<T>{
    return FunResult(this)
}