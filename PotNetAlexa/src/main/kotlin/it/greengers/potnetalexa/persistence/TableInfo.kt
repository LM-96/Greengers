package it.greengers.potnetalexa.persistence

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader

data class TableInfo(
    @SerializedName("table")
    val table : String,

    @SerializedName("user_mail_col")
    val userMailColName : String,

    @SerializedName("br_addr_col")
    val potIdColName : String,
) {
    companion object {
        private const val PERSISTENCE_FILE = "Persistence.json"
        val TABLE_INFO : TableInfo = try {
            val reader = InputStreamReader(javaClass.classLoader.getResourceAsStream(PERSISTENCE_FILE))
            Gson().fromJson(reader, TableInfo::class.java)
        } catch (e : Exception) {
            TableInfo("POTNETALEXATABLE", "USER_MAIL", "POT_ID")
        }

    }
}
