package it.greengers.potserver.core

import it.greengers.potconnectors.utils.withError
import okhttp3.internal.toImmutableList
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

object Settings {

    @JvmStatic val SETTINGS_FILE = "data/settings.props"
    private val props = Properties()

    init {
        withError(load()) {
            loadDefauls()
            persist()
        }
    }

    @JvmStatic fun load() : Error? {
        try {
            props.load(Files.newInputStream(Paths.get(SETTINGS_FILE)))
        } catch (e : Exception) {
            return Error(e)
        }

        return null
    }

    @JvmStatic fun getSetting(name: String) : String {
        return props.getProperty(name)
    }

    @JvmStatic fun addSetting(name: String, value: String) {
        props.setProperty(name, value)
    }

    @JvmStatic fun addSettingAndPersist(name: String, value: String) : List<Error> {
        props.setProperty(name, value)
        return persist()
    }

    @JvmStatic fun persist() : List<Error> {
        val res = mutableListOf<Error>()
        try {
            props.store(
                Files.newOutputStream(Paths.get(SETTINGS_FILE), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE),
                "PotServer settings")
        } catch (e : Exception) {
            res.add(Error(e))
        }

        return res.toImmutableList()
    }

    @JvmStatic fun loadDefauls() {
        props.setProperty("temp-poll-time", "5000")
        props.setProperty("bright-poll-time", "300000")
        props.setProperty("humidity-poll-time", "5000")
        props.setProperty("battery-critical-level", "20")
        props.setProperty("main-server-address", "www.greengerspot.it")
        props.setProperty("main-server-port", "5555")
        persist()
    }

}