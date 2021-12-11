package it.greengers.potnetcore.sensors.polling

import it.greengers.potconnectors.utils.FunResult
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.kotlin.loggerOf

abstract class AbstractPollingJob<T>(val id: String, protected val scope : CoroutineScope) : PollingJob<T> {

    companion object {
        protected val LOGGER = loggerOf(this::class.java)
    }

    private val onPolling = mutableListOf<suspend (T) -> Unit>()
    protected val pollingJob : Job
    private var state = PollingJobState.READY
    private val mutex = Mutex()

    private val stateChanges = Channel<PollingJobState>()
    private val timeChanges = Channel<Long>()
    private val ack = Channel<Unit>()

    init {
        pollingJob = work()
        runBlocking { ack.receive() }
    }

    private fun work() : Job {
        return scope.launch(Dispatchers.IO) {
            var ticker = ticker(5000)
            var value : FunResult<T>
            ack.send(Unit)
            LOGGER.info("AbstractPollingJob[$id] | Started coroutine ${currentCoroutineContext().job}")

            var currState = stateChanges.receive()
            while (currState != PollingJobState.WORKING)
                currState = stateChanges.receive()
            LOGGER.info("AbstractPollingJob[$id] | Start polling")
            while (true) {
                select<Unit> {

                    stateChanges.onReceive {
                        currState = it
                        while(currState != PollingJobState.WORKING) {
                            LOGGER.info("AbstractPollingJob[$id] | Going to state $currState")
                            currState = stateChanges.receive()

                        }
                    }

                    timeChanges.onReceive {
                        ticker.cancel()
                        ticker = ticker(it)
                        LOGGER.info("AbstractPollingJob[$id] | Changed polling time to $it milliseconds")
                    }

                    ticker.onReceive {
                        value = read()
                        if(value.thereIsError()) {
                            LOGGER.error(value.error!!)
                        } else {
                            LOGGER.info("AbstractPollingJob[$id] | Polling value: $value")
                            onPolling.forEach {
                                try {
                                    it.invoke(value.res!!)
                                } catch (e : Exception) {
                                    LOGGER.error("AbstractPollingJob[$id] | Unable to invoke the callback [$it]: ${e.localizedMessage}")
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    abstract fun read() : FunResult<T>

    abstract suspend fun doStart() : Error?
    abstract suspend fun doPause() : Error?
    abstract suspend fun doStop() : Error?

    override fun getJob(): Job {
        return pollingJob
    }

    override fun attachOnPolling(action: suspend (T) -> Unit) {
        onPolling.add(action)
    }

    override fun detachOnPolling(action: suspend (T) -> Unit) {
        onPolling.remove(action)
    }

    override suspend fun start(): Error? {
        mutex.withLock {
            when(state) {
                PollingJobState.READY, PollingJobState.PAUSED, PollingJobState.STOPPED -> {
                    updateState(PollingJobState.WORKING)
                }
                else -> return Error("Unable to start when in state [$state]")
            }
        }

        return null
    }

    override suspend fun pause(): Error? {
        mutex.withLock {
            when(state) {
                PollingJobState.WORKING -> {
                    updateState(PollingJobState.PAUSED)
                }
                else -> return Error("Unable to start when in state [$state]")
            }
        }

        return null
    }

    override suspend fun stop(): Error? {
        mutex.withLock {
            when(state) {
                PollingJobState.READY, PollingJobState.PAUSED, PollingJobState.WORKING -> {
                    updateState(PollingJobState.STOPPED)
                }
                else -> return Error("Unable to start when in state [$state]")
            }
        }

        return null
    }

    override fun getCurrentState(): PollingJobState {
        return state
    }

    override suspend fun waitForNext(): T {
        val chan = Channel<T>()
        val singlePeeker = SinglePollingPeeker(chan, this)

        return chan.receive()
    }

    private suspend fun updateState(state : PollingJobState) {
        when(state) {
            PollingJobState.PAUSED -> doPause()
            PollingJobState.WORKING -> doStart()
            PollingJobState.STOPPED -> doStop()
            else -> {}
        }
        this.state = state
        stateChanges.send(state)
    }

}

private class SinglePollingPeeker<T>(
    private val channel: SendChannel<T>,
    private val pollingJob: PollingJob<T>,
    autoAttach : Boolean = true
) {

    companion object {
        private val LOGGER = loggerOf(this::class.java)
    }

    private val callback : suspend (T) -> Unit = {
        try {
            channel.send(it)
            channel.close()
        } catch (e : Exception) {
            LOGGER.error(e)
        }
        detach()
    }

    init {
        if(autoAttach)
            attach()
    }

    fun attach() {
        pollingJob.attachOnPolling(callback)
    }

    fun detach() {
        pollingJob.detachOnPolling(callback)
    }


}