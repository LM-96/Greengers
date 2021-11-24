package it.greengers.potconnectors.utils

import org.apache.logging.log4j.kotlin.KotlinLogger

inline fun withExceptionToError(toDo : () -> Unit) : Error? {
    try {
        toDo.invoke()
    } catch (e : Exception) {
        return Error(e.stackTraceToString())
    }

    return null
}

inline fun withExceptionToError(logger : KotlinLogger, toDo: () -> Unit) : Error? {
    try {
        toDo.invoke()
    } catch (e : Exception) {
        logger.error(e.stackTraceToString())
        return Error(e.stackTraceToString())
    }

    return null
}

inline fun withExceptionAndErrorToError(toDo: () -> Error?) : Error? {
    return try {
        toDo.invoke()
    } catch (e : Exception) {
        Error(e.stackTraceToString())
    }
}

inline fun withExceptionAndErrorToError(logger : KotlinLogger, toDo: () -> Error?) : Error? {
    return try {
        toDo.invoke()
    } catch (e : Exception) {
        logger.error(e.stackTraceToString())
        Error(e.stackTraceToString())
    }
}

inline fun withNoError(error: Error?, handler: () -> Unit) {
    if(error == null)
        handler.invoke()
}

inline fun withError(error: Error?, handler: (Error) -> Unit) {
    if(error != null)
        handler.invoke(error)
}