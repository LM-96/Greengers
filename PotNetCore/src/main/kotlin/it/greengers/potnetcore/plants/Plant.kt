package it.greengers.potserver.plants

data class Plant(
    val id : String,
    val name : String,
    val scientificName : String,
    val kingdom : String,
    val division : String,
    val clazz : String,
    val family : String,
    val genus : String,
    val species : String,
    val optimalPlantCondition: OptimalPlantCondition
)