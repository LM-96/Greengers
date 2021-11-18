package it.greengers.potconnectors.utils

class FunResult<T> (
    val res : T? = null,
    val error : Error? = null
){

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

}