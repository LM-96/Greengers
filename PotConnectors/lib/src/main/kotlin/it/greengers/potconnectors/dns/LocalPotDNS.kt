package it.greengers.potconnectors.dns

import com.google.gson.Gson
import it.greengers.potconnectors.utils.*
import org.apache.logging.log4j.kotlin.loggerOf
import java.io.*
import java.net.SocketAddress
import java.nio.file.Files
import java.nio.file.StandardOpenOption

object LocalPotDNS : PotDNS {

    @JvmStatic private val DNS_CONFIG_FILE = CONFIG_DIR_PATH.resolve("dns.config")

    @JvmStatic private var ADDRESSES : MutableMap<String, SocketAddress>
    @JvmStatic private var APPLICATION_NAME : String
    @JvmStatic val LOGGER = loggerOf(this::class.java)


    init {
        val loadedConf = try {
            val loaded = Gson().fromJson(Files.newBufferedReader(DNS_CONFIG_FILE), SerializableDNS::class.java)
            loaded.ADDRESSES
            loaded.APPLICATION_NAME
            //Prevent unhandled NullPointerException
            LOGGER.info("Loaded application DNS name: \"${loaded.APPLICATION_NAME}\"")
            loaded
        } catch (e : Exception) {
            LOGGER.error(e)
            val newConf = SerializableDNS()
            LOGGER.info("Generated default configuration $newConf")
            makeBackup(newConf)
            newConf
        }

        this.ADDRESSES = loadedConf.ADDRESSES
        this.APPLICATION_NAME = loadedConf.APPLICATION_NAME
    }

    @JvmStatic fun setApplicationName(name : String) : Error? {
        return if(APPLICATION_NAME == "unknown") {
            APPLICATION_NAME = name
            null
        } else {
            LOGGER.warn("Name is already set to \"$APPLICATION_NAME\": unable to change this name")
            Error("Name is already set to \"$APPLICATION_NAME\": unable to change this name")
        }
    }

    @JvmStatic fun getApplicationName() : String {
        return this.APPLICATION_NAME
    }

    override fun resolve(name: String): FunResult<SocketAddress> {
        return if(ADDRESSES.containsKey(name))
            FunResult(ADDRESSES[name]!!)
        else
            FunResult(Error("Unknown host"))
    }

    override fun registerOrUpdate(name: String, address: SocketAddress) {
        ADDRESSES[name] = address
        LOGGER.info("Added entry [\"$name\"-{$address}] to local DNS")
    }

    override fun delete(name: String) {
        val address = ADDRESSES.remove(name)
        LOGGER.info("Removed entry [\"$name\"-{$address}] from local DNS")
    }

    override fun persists(outputStream: OutputStream) : Error? {
        val gson = Gson()
        val out = BufferedWriter(OutputStreamWriter(outputStream))

        return withExceptionToError(LOGGER) {
            out.write(gson.toJson(ADDRESSES))
            out.flush()
            out.close()
        }
    }

    private fun makeBackup(serializableDNS: SerializableDNS) : Error? {
        withError(createConfigDirectoryIfNotExists()) {
            return it
        }

        return withExceptionToError(LOGGER) {
            val writer = Files.newBufferedWriter(DNS_CONFIG_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
            Gson().toJson(serializableDNS, writer)
            LOGGER.info("Written actual DNS configuration into config file [$DNS_CONFIG_FILE]")
            writer.flush()
            writer.close()
        }
    }

    fun makeBackup(): Error? {
        return makeBackup( SerializableDNS(APPLICATION_NAME, ADDRESSES))
    }

    override fun load(inputStream: InputStream, append : Boolean) : Error? {
        val gson = Gson()
        val input = BufferedReader(InputStreamReader(inputStream))
        if(!append) {
            clear()
        }

        return withExceptionToError(LOGGER) {
            val loaded = gson.fromJson<MutableMap<String, SocketAddress>>(input, MutableMap::class.java)
            ADDRESSES.putAll(loaded)
        }
    }

    override fun clear() {
        ADDRESSES.clear()
        LOGGER.info("Local DNS is now cleaned")
    }

}

private class SerializableDNS(
    val APPLICATION_NAME : String = "unknown",
    val ADDRESSES : MutableMap<String, SocketAddress> = mutableMapOf(),
) {

    override fun toString(): String {
        return "SerializableDNS(APPLICATION_NAME='$APPLICATION_NAME', ADDRESSES=$ADDRESSES)"
    }
}

fun resolveLocal(name : String) : FunResult<SocketAddress> {
    return LocalPotDNS.resolve(name)
}