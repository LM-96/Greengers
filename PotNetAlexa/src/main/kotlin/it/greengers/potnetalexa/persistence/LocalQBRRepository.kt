package it.lm96.qbrtools.persistence

import it.lm96.qbrtools.exposers.utils.printRedln
import java.util.*

class LocalQBRRepository : QBRRepository {

    val entries = mutableMapOf<String, QBREntry>()

    init {
        printRedln("LocalQBRRepository | All invocation on this repo will have no persistence")
    }

    override fun create(entry: QBREntry) {
        entries[entry.USER_MAIL] = entry
        printRedln("LocalQBRRepository | The entry will not be persisted [$entry]")
    }

    override fun retrieve(userMail: String): Optional<QBREntry> {
        printRedln("LocalQBRRepository | No entry will be persistently retrieved")
        return Optional.ofNullable(entries[userMail])
    }

    override fun update(entry: QBREntry) {
        if(!entries.containsKey(entry.USER_MAIL))
            throw PersistenceException("No elements with Primary Key ${entry.USER_MAIL}")
        else {
            entries[entry.USER_MAIL] = entry
            printRedln("LocalQBRRepository | The entry will not be persistently updated [$entry]")
        }
    }

    override fun delete(userMail: String): Optional<QBREntry> {
        return if(!entries.containsKey(userMail))
            Optional.empty()
        else {
            val res = entries.remove(userMail)
            printRedln("LocalQBRRepository | No entry will be persistently deleted")
            Optional.of(res!!)
        }
    }

    override fun createOrUpdate(entry: QBREntry) {
        entries[entry.USER_MAIL] = entry
        printRedln("LocalQBRRepository | The entry will not be persisted [$entry]")
    }

    override fun getAll(): List<QBREntry> {
        printRedln("LocalQBRRepository | No entry will be retrieved")
        return entries.values.toList()
    }

}