package it.greengers.potconnectors.dns

import it.greengers.potconnectors.utils.FunResult
import java.io.InputStream
import java.io.OutputStream
import java.net.SocketAddress
import java.nio.file.Files
import java.nio.file.Paths

interface PotDNS {

    companion object {
        @JvmStatic val SYSTEM_DNS : PotDNS = LocalPotDNS
    }

    /**
     * Resolve a simbolic name to a SocketAddress
     *
     * @param name the name to be resolved
     * @return a FunResult containing the address of the name or an error if something goes wrong
     */
    suspend fun resolve(name : String) : FunResult<SocketAddress>

    /**
     * Register a name and its address. If the name is already present in the DNS table,
     * this method update the address
     *
     * @param name the name to be registered
     * @param address the address referred by the name
     */
    suspend fun registerOrUpdate(name : String, address : SocketAddress)

    /**
     * Delete a name and its address from the DNS table
     */
    suspend fun delete(name : String)

    /**
     * Save a representation containing all of the saved address to
     * an OutputStream
     *
     * @param outputStream the OutputStream that will be used
     * @return null if success or an error otherwise
     */
    suspend fun persists(outputStream : OutputStream) : Error?

    /**
     * Load all DNS entries previously saved with the *persists()* method.
     *
     * @param inputStream the InputStream used to read the entries
     * @param append a boolean that is true if the readed entries must be appended
     * @return null if success or an error otherwise
     */
    suspend fun load(inputStream : InputStream, append: Boolean = false) : Error?

    /**
     * Clear the DNS. After this call, the DNS has no entries
     */
    suspend fun clear()

}