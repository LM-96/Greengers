package it.greengers.potconnectors.utils

import it.greengers.potconnectors.dns.LocalPotDNS
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.ApplMessageType
import it.unibo.kactor.MsgUtil
import org.apache.logging.log4j.kotlin.KotlinLogger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

val CONFIG_DIR_PATH = Paths.get("conf")

fun buildValidApplMessage(msg : String) : FunResult<ApplMessage> {
    return try {
        FunResult(ApplMessage(msg))
    } catch (e : Exception) {
        return FunResult(Error("Invalid actor application message format\n${e.localizedMessage}"))
    }
}

fun buildApplMessage(destActor: String, msgType: ApplMessageType, msgName: String, vararg payloadArgs: String) : FunResult<ApplMessage> {
    val content = "$msgName(${payloadArgs.map { it.trim() }.joinToString(separator = ",")})"
    val applMsg : ApplMessage = when(msgType) {
        ApplMessageType.dispatch ->
            MsgUtil.buildDispatch(LocalPotDNS.getApplicationName(), msgName, content, destActor)
        ApplMessageType.event ->
            MsgUtil.buildEvent(LocalPotDNS.getApplicationName(), msgName, content)
        ApplMessageType.request ->
            MsgUtil.buildRequest(LocalPotDNS.getApplicationName(), msgName, content, destActor)
        ApplMessageType.reply ->
            MsgUtil.buildReply(LocalPotDNS.getApplicationName(), msgName, content, destActor)

        else -> {return FunResult(Error("Unsupported ApplMessageType"))
        }
    }

    return FunResult(applMsg)
}

fun createConfigDirectoryIfNotExists() : Error? {
    return createDirectoryIfNotExists(CONFIG_DIR_PATH)
}

fun createDirectoryIfNotExists(path : Path) : Error? {
    return withExceptionToError {
        if(!Files.exists(path) || !Files.isDirectory(path))
            Files.createDirectories(path)
    }
}

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

inline fun withNullError(error: Error?, toDo: () -> Unit) {
    if(error == null)
        toDo.invoke()
}

inline fun withNotNullError(error: Error?, toDo: (Error) -> Unit) {
    if(error != null)
        toDo.invoke(error)
}