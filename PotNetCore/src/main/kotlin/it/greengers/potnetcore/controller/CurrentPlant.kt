package it.greengers.potnetcore.controller

import it.greengers.potconnectors.utils.withExceptionToError
import it.greengers.potnetcore.sensors.SensorType
import it.greengers.potserver.plants.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object CurrentPlant {
    @JvmStatic private val CURRENT_PLANT_FILE = Paths.get("current_plant.json")

    @JvmStatic lateinit var CURRENT_PLANT : Plant
        private set
    val STATE : PlantState = PlantState()
    private val mutex = Mutex()

    @JvmStatic
    suspend fun withSafeCurrentPlant(action : (Plant) -> Unit) {
        mutex.withLock {
            action.invoke(CURRENT_PLANT)
        }
    }

    @JvmStatic
    suspend fun <T> safeProduceWithCurrentPlant(action : (Plant) -> T) : T {
        mutex.withLock {
            return action.invoke(CURRENT_PLANT)
        }
    }

    @JvmStatic
    suspend fun withSafeState(action : (PlantState) -> Unit) {
        mutex.withLock {
            action.invoke(STATE)
        }
    }

    @JvmStatic
    suspend fun <T> safeProduteWithState(action : (PlantState) -> T) : T {
        mutex.withLock {
            return action.invoke(STATE)
        }
    }

    @JvmStatic
    suspend fun <T> updateState(valueType: SensorType, value : T) {
        mutex.withLock {
            when(valueType) {
                SensorType.TEMPERATURE -> STATE.temperature = (value as Double)
                SensorType.HUMIDITY -> STATE.humidity = (value as Double)
                SensorType.BRIGHTNESS -> STATE.brightness = (value as Double)
            }
        }
    }

    @JvmStatic
    suspend fun loadCurrentPlant() {
        mutex.withLock {
            if(Files.exists(CURRENT_PLANT_FILE)) {
                CURRENT_PLANT = try {
                    GSON.fromJson(Files.newBufferedReader(CURRENT_PLANT_FILE), Plant::class.java)
                } catch (e : Exception) {
                    EMPTY_PLANT
                }
            }
            CURRENT_PLANT = EMPTY_PLANT
        }
    }

    @JvmStatic
    suspend fun changeCurrentPlant(plant: Plant) {
        mutex.withLock {
            CURRENT_PLANT = plant
            STATE.reset()
            doPersist()
        }
    }

    @JvmStatic
    suspend fun persist() : Error? {
        mutex.withLock {
            return doPersist()
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