package it.greengers.potconnectors.utils

import kotlinx.coroutines.*
import it.greengers.potconnectors.concurrency.newCondition
import kotlinx.coroutines.sync.Mutex

fun main(args : Array<String>) {
    val mutex = Mutex()
    val cond = mutex.newCondition()

    val job1 = GlobalScope.launch {
        println("job1 is going to sleep")
        mutex.lock()
        cond.wait()
        println("job1 signaled")
        mutex.unlock()
        delay(10000)
    }

    val job2 = GlobalScope.launch {
        delay(2000)
        cond.signalAll()
        delay(2000)
    }

    val job3 = GlobalScope.launch {
        println("job3 is going to sleep")
        mutex.lock()
        cond.wait()
        mutex.unlock()
        println("job3 signaled")
        delay(10000)
    }

    runBlocking {
        job1.join()
        job2.join()
        job3.join()
    }
}