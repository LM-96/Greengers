package it.greengers.potconnectors.utils

import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream

/**
 * Write a line then flush the stream
 */
@Throws(IOException::class)
fun BufferedWriter.writeLineAndFlush(str : String) {
    this.write(str)
    this.newLine()
    this.flush()
}