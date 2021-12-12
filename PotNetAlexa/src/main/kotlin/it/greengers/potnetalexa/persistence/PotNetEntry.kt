package it.lm96.qbrtools.persistence

data class PotNetEntry(
    val USER_MAIL : String,
    var POT_ID : String,
) {
    companion object {
        val TABLE_NAME = ""
        val USER_MAIL_COL = "USER_MAIL"
        val BR_ADDRESS_COL = "POT_ID"
    }
}
