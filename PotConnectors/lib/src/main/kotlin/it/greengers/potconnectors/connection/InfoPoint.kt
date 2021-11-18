package it.greengers.potconnectors.connection

object InfoPoint {

    @JvmStatic private var applicationHostName : String = "unknown"

    @JvmStatic fun setApplicationHostName(applicationHostName : String) {
        this.applicationHostName = applicationHostName
    }

    @JvmStatic fun getApplicationHostName() : String {
        return this.applicationHostName
    }

}