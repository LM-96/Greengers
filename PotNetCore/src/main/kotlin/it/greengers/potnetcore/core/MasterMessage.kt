package it.greengers.potnetcore.core

data class MasterMessage(
    val destination : String,
    val sender : String,
    val messageType : MasterMessageType,
    val messageName : String,
    val payloadArgs : Array<String>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MasterMessage) return false

        if (destination != other.destination) return false
        if (sender != other.sender) return false
        if (messageType != other.messageType) return false
        if (messageName != other.messageName) return false
        if (!payloadArgs.contentEquals(other.payloadArgs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + sender.hashCode()
        result = 31 * result + messageType.hashCode()
        result = 31 * result + messageName.hashCode()
        result = 31 * result + payloadArgs.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "MasterMessage(destination='$destination', sender='$sender', messageType=$messageType, messageName='$messageName', payloadArgs=${payloadArgs.contentToString()})"
    }
}

enum class MasterMessageType {
    REQUEST, REPLY, EVENT, DISPATCH
}