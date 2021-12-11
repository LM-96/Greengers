package it.lm96.qbrtools.persistence

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader

data class TableInfo(
    @SerializedName("table")
    val table : String,

    @SerializedName("user_mail_col")
    val userMailColName : String,

    @SerializedName("br_addr_col")
    val brAddressColName : String,

    @SerializedName("br_port_col")
    val brPortColName : String
) {
    companion object {
        @JvmStatic private val PERSISTENCE_FILE = "Persistence.json"
        @JvmStatic val TABLE_INFO : TableInfo
        init {
            val reader = InputStreamReader(javaClass.classLoader.getResourceAsStream(PERSISTENCE_FILE))
            TABLE_INFO = Gson().fromJson(reader, TableInfo::class.java)
        }
    }
}
