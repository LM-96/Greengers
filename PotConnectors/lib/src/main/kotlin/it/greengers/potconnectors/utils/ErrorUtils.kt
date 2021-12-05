package it.greengers.potconnectors.utils

import org.apache.logging.log4j.kotlin.KotlinLogger

/**
 * Intercepts all exceptions thrown by a function, then return
 * the caught exception as a Kotlin error
 *
 * @param toDo the function that can throw the exception
 * @return the exception as a kotlin error or null if no exception is thrown
 */
inline fun withExceptionToError(toDo : () -> Unit) : Error? {
    try {
        toDo.invoke()
    } catch (e : Exception) {
        return Error(e.stackTraceToString())
    }

    return null
}

/**
 * Intercept all exceptions thrown by a producer, then return a FunResult
 * containing the produced value or an Error containing the caught exception
 *
 * @param producer the function that produces a result of type *T*
 * @return the FunResult object containing the value if correctly produced
 * or an error containing the thrown exception
 */
inline fun <T> withExceptionToFunResult(producer : () -> T) : FunResult<T> {
    return try {
        FunResult(producer.invoke())
    } catch (e : Exception) {
        FunResult(e)
    }
}

/**
 * Intercept all exceptions thrown by a producer, then return the FunResult
 * produced by the function or an Error containing the caught exception
 *
 * @param producer the function that produces a FunResult
 * @return the FunResult object containing the value if correctly produced
 * or an error containing the thrown exception
 */
inline fun <T> interceptExceptionToFunResult(toDo : () -> FunResult<T>) : FunResult<T> {
    return try {
        toDo.invoke()
    } catch (e : Exception) {
        return FunResult(e)
    }
}

/**
 * Intercept the first exception thrown by a procedure and log it using a KotlinLogger
 * @param logger the KotlinLogger object that will be used to log the exception
 * @param toDo the procedure that should thrown an exception
 */
inline fun withLoggedException(logger : KotlinLogger, toDo: () -> Unit) {
    try {
        toDo.invoke()
    } catch (e : Exception) {
        logger.error(e.stackTraceToString())
    }
}

/**
 * Intercept the first exception thrown by a procedure and log it using a KotlinLogger
 * @param logger the KotlinLogger object that will be used to log the exception
 * @param prologue the prologue that will be added to the begin of the log line
 * @param toDo the procedure that should thrown an exception
 */
inline fun withLoggedException(logger : KotlinLogger, prologue: String, toDo: () -> Unit) {
    try {
        toDo.invoke()
    } catch (e : Exception) {
        logger.error("$prologue ${e.stackTraceToString()}")
    }
}

/**
 * Intercept the first exception thrown by an *errorable function* and log it using a KotlinLogger.
 * If the the function returns without throwing an exception and with a non-null error, the error
 * will be logged.
 * @param logger the KotlinLogger object that will be used to log the exception
 * @param toDo the function that should thrown an exception or return a non-null error
 */
inline fun withLoggedExceptionAndError(logger : KotlinLogger, toDo: () -> Error?) {
    try {
        val err = toDo.invoke()
        if(err != null)
            logger.error(err)
    } catch (e : Exception) {
        logger.error(e.stackTraceToString())
    }
}

/**
 * Intercept the first exception thrown by an *errorable function* and log it using a KotlinLogger.
 * If the the function returns without throwing an exception and with a non-null error, the error
 * will be logged.
 * @param logger the KotlinLogger object that will be used to log the exception
 * @param prologue the prologue that will be added to the begin of the log line
 * @param toDo the function that should thrown an exception or return a non-null error
 */
inline fun withLoggedExceptionAndError(logger : KotlinLogger, prologue: String, toDo: () -> Error?) {
    try {
        val err = toDo.invoke()
        if(err != null)
            logger.error("$prologue ${err.stackTraceToString()}")
    } catch (e : Exception) {
        logger.error("$prologue ${e.stackTraceToString()}")
    }
}

/**
 * Intercepts all exceptions thrown by a function, then return
 * the caught exception as a Kotlin error, logging it
 *
 * @param logger the KotlinLogger object that will be used to log the exception
 * @param toDo the function that can throw the exception
 * @return the exception as a kotlin error or null if no exception is thrown
 */
inline fun withExceptionToError(logger : KotlinLogger, toDo: () -> Unit) : Error? {
    try {
        toDo.invoke()
    } catch (e : Exception) {
        logger.error(e.stackTraceToString())
        return Error(e.stackTraceToString())
    }

    return null
}

/**
 * Intercept the first exception thrown by an *errorable function* returning it as a kotlin Error.
 * If the the function returns without throwing an exception and with a non-null error, then this error
 * will be returned
 * @param toDo the function that should thrown an exception or return a non-null error
 * @return an error or null if no error or exception are thrown by the function
 */
inline fun withExceptionAndErrorToError(toDo: () -> Error?) : Error? {
    return try {
        toDo.invoke()
    } catch (e : Exception) {
        Error(e.stackTraceToString())
    }
}

/**
 * Intercept the first exception thrown by an *errorable function* returning it as a kotlin Error.
 * If the the function returns without throwing an exception and with a non-null error, then this error
 * will be returned. Any exception or error will be logged
 * @param toDo the function that should thrown an exception or return a non-null error
 * @return an error or null if no error or exception are thrown by the function
 */
inline fun withExceptionAndErrorToError(logger : KotlinLogger, toDo: () -> Error?) : Error? {
    return try {
        val err = toDo.invoke()
        if(err != null)
            logger.error(err.stackTraceToString())
        err
    } catch (e : Exception) {
        logger.error(e.stackTraceToString())
        Error(e.stackTraceToString())
    }
}

/**
 * Invokes an handler if the error passed as a parameter is non-null
 *
 * @param error the nullable error
 * @param handler the function that will be invoked if the error is not null
 */
inline fun withNoError(error: Error?, handler: () -> Unit) {
    if(error == null)
        handler.invoke()
}

/**
 * Invokes an handler if the error passed as a parameter is non-null.
 * The handler is invoked with the non-null error
 *
 * @param error the nullable error
 * @param handler the function that will be invoked if the error is not null
 */
inline fun withError(error: Error?, handler: (Error) -> Unit) {
    if(error != null)
        handler.invoke(error)
}

/**
 * Invoke the action if the passed object is null
 *
 * @param obj the object that could be null
 * @param action the action to be performed if the object is null
 */
inline fun withNullValue(obj : Any?, action: () -> Unit) {
    if(obj==null)
        action.invoke()
}

/**
 * Invoke the action if the passed object is not null
 *
 * @param obj the object that could be null
 * @param action the action to be performed if the object is not null
 */
inline fun <T> withNotNullValue(obj : T?, action: (T) -> Unit) {
    if(obj!=null)
        action.invoke(obj)
}