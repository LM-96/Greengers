package it.lm96.qbrtools.persistence

import java.util.*
import kotlin.jvm.Throws

interface QBRRepository {

    @Throws(PersistenceException::class)
    fun create(entry: QBREntry)

    @Throws(PersistenceException::class)
    fun retrieve(userMail: String): Optional<QBREntry>

    @Throws(PersistenceException::class)
    fun update(entry: QBREntry)

    @Throws(PersistenceException::class)
    fun delete(userMail: String): Optional<QBREntry>

    @Throws(PersistenceException::class)
    fun createOrUpdate(entry: QBREntry)

    @Throws(PersistenceException::class)
    fun getAll(): List<QBREntry>
}