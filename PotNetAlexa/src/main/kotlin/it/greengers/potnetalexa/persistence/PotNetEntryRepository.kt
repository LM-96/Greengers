package it.lm96.qbrtools.persistence

import java.util.*
import kotlin.jvm.Throws

interface PotNetEntryRepository {

    @Throws(PersistenceException::class)
    fun create(entry: PotNetEntry)

    @Throws(PersistenceException::class)
    fun retrieve(userMail: String): Optional<PotNetEntry>

    @Throws(PersistenceException::class)
    fun update(entry: PotNetEntry)

    @Throws(PersistenceException::class)
    fun delete(userMail: String): Optional<PotNetEntry>

    @Throws(PersistenceException::class)
    fun createOrUpdate(entry: PotNetEntry)

    @Throws(PersistenceException::class)
    fun getAll(): List<PotNetEntry>
}