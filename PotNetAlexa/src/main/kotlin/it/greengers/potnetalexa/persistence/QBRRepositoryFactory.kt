package it.lm96.qbrtools.persistence

object QBRRepositoryFactory {

    fun createRepository(type : QBRRepositoryType) : QBRRepository {
        return when(type) {
            QBRRepositoryType.DYNAMO_DB -> return DDBRepository()
            QBRRepositoryType.LOCAL -> return LocalQBRRepository()
        }
    }

}

enum class QBRRepositoryType {
    DYNAMO_DB, LOCAL
}