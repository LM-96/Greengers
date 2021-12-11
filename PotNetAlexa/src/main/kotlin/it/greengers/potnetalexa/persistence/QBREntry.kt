package it.lm96.qbrtools.persistence

data class QBREntry(
    val USER_MAIL : String,
    var BASICROBOT_ADDRESS : String,
    var BASICROBOT_PORT : Int
) {
    companion object {
        val TABLE_NAME = ""
        val USER_MAIL_COL = "USER_MAIL"
        val BR_ADDRESS_COL = "BR_ADDRESS"
        val BR_PORT_COL = "BR_PORT"
    }
}
