package it.greengers.potconnectors.utils

import it.greengers.potconnectors.dns.LocalPotDNS
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.ApplMessageType
import it.unibo.kactor.MsgUtil
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

