package it.greengers.potserver.plants

import it.greengers.potnetcore.sensors.SensorType
import it.greengers.potnetcore.sensors.ValueRange

data class OptimalPlantCondition(
    val temperatureRange : ValueRange<Double>,
    val humidityRange : ValueRange<Double>,
    val brightnessRange : ValueRange<Double>
) {

    fun fromSensorType(sensorType: SensorType) : ValueRange<Double> {
        return when(sensorType) {
            SensorType.TEMPERATURE -> temperatureRange
            SensorType.HUMIDITY -> humidityRange
            SensorType.BRIGHTNESS -> brightnessRange
        }
    }
}