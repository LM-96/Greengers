package it.greengers.potserver.plants

data class PlantState(
    var temperature: Double = -273.16,
    var humidity: Double = -1.0,
    var brightness: Double = -1.0,
    var battery: Double = -1.0
)