package it.lm96.qbrtools.persistence

import it.greengers.potnetalexa.persistence.DDBRepository

object QBRRepositoryFactory {

    fun createRepository(type : QBRRepositoryType) : PotNetEntryRepository {
        return when(type) {
            QBRRepositoryType.DYNAMO_DB -> return DDBRepository()
        }
    }

}

enum class QBRRepositoryType {
    DYNAMO_DB
}