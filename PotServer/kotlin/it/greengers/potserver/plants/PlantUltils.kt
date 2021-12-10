package it.greengers.potserver.plants

import com.google.gson.Gson
import it.greengers.potserver.core.CurrentPlant
import it.greengers.potserver.sensors.SensorFactory
import it.greengers.potserver.sensors.SensorType
import it.greengers.potserver.sensors.ValueRange
import java.lang.NumberFormatException

private data class CurrentPlantWithState(
    val plant : Plant,
    val state : PlantState
)

object PlantUtils {
    fun updateCurrentPlantState(sensorId : String, value : String) : Boolean {
        return CurrentPlant.STATE.update(sensorId, value)
    }

    fun updateState(state : PlantState, sensorId : String, value : String) : Boolean {
        return state.update(sensorId, value)
    }

    fun currentPlantWithStateToJSON() : String {
        return CurrentPlant.withStateToJSON()
    }

    fun changeCurrentPlantFromJSON(json : String) {
        CurrentPlant.changeCurrentPlantFromJSON(json)
    }

    fun stateToJSON(state : PlantState) : String {
        return state.toJson()
    }
}

val GSON = Gson()
val EMPTY_PLANT = Plant("NO_PLANT", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN",
    OptimalPlantCondition(ValueRange<Double>(Double.MIN_VALUE, Double.MAX_VALUE), ValueRange<Double>(Double.MIN_VALUE, Double.MAX_VALUE), ValueRange<Double>(Double.MIN_VALUE, Double.MAX_VALUE))
)

fun CurrentPlant.withStateToJSON() : String {
    val data = CurrentPlantWithState(CURRENT_PLANT, STATE)
    return GSON.toJson(data)
}

fun CurrentPlant.changeCurrentPlantFromJSON(json : String) {
    changeCurrentPlant(GSON.fromJson(json, Plant::class.java))
}

fun PlantState.toJson() : String {
    return GSON.toJson(this)
}

fun PlantState.reset() {
    temperature = -273.16
    humidity = -1.0
    brightness = -1.0
    battery = -1.0
}

fun PlantState.update(sensorId : String, value : String) : Boolean {
    val type = SensorFactory.getSensorType(sensorId)

    if(type == null) {
        println("PlantState | Requested impracticable update from sensor $sensorId with value $value")
        return false
    }

    try {
        when(type) {
            SensorType.TEMPERATURE -> temperature = value.toDouble()
            SensorType.HUMIDITY -> humidity = value.toDouble()
            SensorType.BRIGHTNESS -> brightness = value.toDouble()
        }
        return true
    } catch (nfe : NumberFormatException) {
        println("PlantState | Unable to cast to number for update [sensorId=$sensorId, value=$value]")
    }
    return false
}