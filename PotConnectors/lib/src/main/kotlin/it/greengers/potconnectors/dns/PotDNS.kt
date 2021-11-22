package it.greengers.potconnectors.dns

import it.greengers.potconnectors.utils.FunResult
import java.io.InputStream
import java.io.OutputStream
import java.net.SocketAddress

interface PotDNS {

    fun resolve(name : String) : FunResult<SocketAddress>
    fun registerOrUpdate(name : String, address : SocketAddress)
    fun delete(name : String)

    fun persists(outputStream : OutputStream) : Error?
    fun load(inputStream : InputStream, append: Boolean = false) : Error?
    fun clear()

}