package it.greengers.potnetcore.controller

import it.greengers.potconnectors.utils.withExceptionToError
import it.greengers.potnetcore.sensors.SensorType
import it.greengers.potserver.plants.*
import kotlinx.coroutines.sync.Mutex
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object CurrentPlant {
    private val CURRENT_PLANT_FILE = Paths.get("current_plant.json")

    var CURRENT_PLANT : Plant = loadCurrentPlant()
        private set
    val STATE : PlantState = PlantState()
    val mutex = Mutex()


    suspend fun withSafeCurrentPlant(action : (Plant) -> Unit) {
        mutex.lock()
        try {
            action.invoke(CURRENT_PLANT)
        } finally {
            mutex.unlock()
        }
    }

    suspend fun <T> safeProduceWithCurrentPlant(action : (Plant) -> T) : T {
        mutex.lock()
        try {
            return action.invoke(CURRENT_PLANT)
        } finally {mutex.unlock()}
    }

    suspend fun withSafeState(action : (PlantState) -> Unit) {
        mutex.lock()
        try {
            action.invoke(STATE)
        } finally {mutex.unlock()}
    }

    suspend fun <T> safeProduteWithState(action : (PlantState) -> T) : T {
        mutex.lock()
        try {
            return action.invoke(STATE)
        } finally { mutex.unlock() }
    }

    suspend fun <T> updateState(valueType: SensorType, value : T) {
        mutex.lock()
        try {
            when(valueType) {
                SensorType.TEMPERATURE -> STATE.temperature = value as Double
                SensorType.HUMIDITY -> STATE.humidity = value as Double
                SensorType.BRIGHTNESS -> STATE.brightness = value as Double
            }
        } finally {
            mutex.unlock()
        }
    }

    fun loadCurrentPlant() : Plant{
        if(Files.exists(CURRENT_PLANT_FILE)) {
            return try {
                GSON.fromJson(Files.newBufferedReader(CURRENT_PLANT_FILE), Plant::class.java)
            } catch (e : Exception) {
                EMPTY_PLANT
            }
        }
        return EMPTY_PLANT
    }

    suspend fun changeCurrentPlant(plant: Plant) {
        mutex.lock()
        try {
            CURRENT_PLANT = plant
            STATE.reset()
            doPersist()
        } finally {
            mutex.unlock()
        }
    }

    suspend fun persist() : Error? {
        mutex.lock()
        try {
            return doPersist()
        } finally {
            mutex.unlock()
        }
    }

    private fun doPersist() : Error? {
        return withExceptionToError {
            val writer = Files.newBufferedWriter(CURRENT_PLANT_FILE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
            GSON.toJson(CURRENT_PLANT, writer)
            writer.flush()
            writer.close()
        }
    }

}