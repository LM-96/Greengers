package it.greengers.potserver.core

import it.greengers.potconnectors.utils.withExceptionToError
import it.greengers.potserver.plants.GSON
import it.greengers.potserver.plants.Plant
import it.greengers.potserver.plants.PlantState
import it.greengers.potserver.plants.reset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object CurrentPlant {
    @JvmStatic private val CURRENT_PLANT_FILE = Paths.get("current_plant.json")

    @JvmStatic lateinit var CURRENT_PLANT : Plant
        private set
    val STATE : PlantState = PlantState()

    @JvmStatic fun loadCurrentPlant() {
        
    }

    @JvmStatic fun changeCurrentPlant(plant: Plant) {
        CURRENT_PLANT = plant
        STATE.reset()
    }

    @JvmStatic fun persist() : Error? {
        return withExceptionToError {
            val writer = Files.newBufferedWriter(CURRENT_PLANT_FILE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
            GSON.toJson(CURRENT_PLANT, writer)
            writer.flush()
            writer.close()
        }
    }

}