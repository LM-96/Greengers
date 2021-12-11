package it.greengers.potnetcore.sensors.polling

abstract class PollingListener<T>(
    val job : SensorPollingJob<T>,
    autoAttach : Boolean = true) {

    private val callback = this::onPolling
    init {
        if(autoAttach)
            attach()
    }

    fun attach() {
        job.attachOnPolling(callback)
    }
    fun detach() {
        job.detachOnPolling(callback)
    }

    abstract suspend fun onPolling(value : T)

}