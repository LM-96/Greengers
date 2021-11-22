package it.greengers.potconnectors.dns

import com.google.gson.Gson
import it.greengers.potconnectors.utils.FunResult
import org.apache.logging.log4j.kotlin.loggerOf
import java.io.*
import java.net.SocketAddress

object LocalPotDNS : PotDNS {

    @JvmStatic private val ADDRESSES = mutableMapOf<String, SocketAddress>()
    @JvmStatic val LOGGER = loggerOf(this::class.java)

    override fun resolve(name: String): FunResult<SocketAddress> {
        return if(ADDRESSES.containsKey(name))
            FunResult(ADDRESSES[name]!!)
        else
            FunResult(error = Error("Unknown host"))
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
        try {
            out.write(gson.toJson(ADDRESSES))
            out.flush()
            out.close()
        } catch (e : Exception) {
            LOGGER.error(e)
            return Error(e)
        }

        return null
    }

    override fun load(inputStream: InputStream, append : Boolean) : Error? {
        val gson = Gson()
        val input = BufferedReader(InputStreamReader(inputStream))
        if(!append) {
            clear()
        }

        try {
            val loaded = gson.fromJson<MutableMap<String, SocketAddress>>(input, MutableMap::class.java)
            ADDRESSES.putAll(loaded)
        } catch (e : Exception) {
            LOGGER.error(e)
            return Error(e)
        }

        return null
    }

    override fun clear() {
        ADDRESSES.clear()
        LOGGER.info("Local DNS is now cleaned")
    }

}