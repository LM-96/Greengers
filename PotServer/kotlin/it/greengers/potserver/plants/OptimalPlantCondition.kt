package it.greengers.potserver.plants

import it.greengers.potserver.sensors.ValueRange

class OptimalPlantCondition(
    val temperatureRange : ValueRange<Double>,
    val humidityRange : ValueRange<Double>,
    val brightnessRange : ValueRange<Double>
) {
}