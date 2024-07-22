package com.github.wanasit.kotori.utils

import com.github.wanasit.kotori.mecab.MeCabConnectionCost
import io.ktor.util.readShort
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.decode
import okio.Buffer
import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer
import okio.use

object IOUtils {
    fun writeStringArray(outputStream: Sink, value: Array<String>, includeSize: Boolean = true) {
        val dataOutputStream = outputStream.buffer()
        if (includeSize) {
            dataOutputStream.writeInt(value.size)
        }

        val lengthArray = value.map { it.length }.toIntArray()
        writeIntArray(outputStream, lengthArray, includeSize = false)
        value.forEach {
            dataOutputStream.writeUtf8(it)
        }
    }

    fun writeIntArray(outputStream: Sink, value: IntArray, includeSize: Boolean = true) {
        val dataOutputStream = outputStream.buffer()
        if (includeSize) {
            dataOutputStream.writeInt(value.size)
        }


        value.forEach { dataOutputStream.writeInt(it) }
    }

    fun writeShortArray(outputStream: Sink, value: ShortArray, includeSize: Boolean = true) {
        val dataOutputStream = outputStream.buffer()
        if (includeSize) {
            dataOutputStream.writeInt(value.size)
        }

        value.forEach { dataOutputStream.writeShort(it.toInt()) }
    }

    fun writeInt(outputStream: Sink, value: Int) {
        val dataOutputStream = outputStream.buffer()
        dataOutputStream.writeInt(value)
    }

    fun readStringWithCharset(inputStream: Source, charset: Charset): String {
        val bytes: ByteArray =
            inputStream.use {
                it.buffer().readByteArray()
            }

        val buffer: kotlinx.io.Buffer = kotlinx.io.Buffer()
        buffer.write(bytes)

        return charset.newDecoder().decode(buffer)
    }

    fun readStringArray(inputStream: Source): Array<String>  {
        val dataInputStream = inputStream.buffer()
        val size = dataInputStream.readInt()
        return readStringArray(inputStream, size)
    }

    fun readStringArray(inputStream: Source, size: Int): Array<String> {
        val lengthArray = readIntArray(inputStream, size)
        return readStringArray(inputStream, lengthArray)
    }

    fun readStringArray(inputStream: Source, stringLengthArray: IntArray): Array<String> {
        val dataInputStream = inputStream.buffer()
        val output = Array(stringLengthArray.size) {
            val bytes = dataInputStream.readByteArray(stringLengthArray[it] * 2)
            bytes.decodeToString()
        }

        return output
    }


    fun readShortArray(inputStream: Source): ShortArray {
        val dataInputStream = inputStream.buffer()
        val size = dataInputStream.readInt()
        return readShortArray(inputStream, size)
    }

    @OptIn(InternalAPI::class)
    fun readShortArray(inputStream: Source, size: Int): ShortArray {
        val dataInputStream = inputStream.buffer()

        val bytes = dataInputStream.readByteArray(size * 2)
        val output = ShortArray(size) { bytes.readShort(it) }

        return output
    }

    fun readIntArray(inputStream: Source): IntArray {
        val dataInputStream = inputStream.buffer()
        val size = dataInputStream.readInt()
        return readIntArray(inputStream, size)
    }

    fun readIntArray(inputStream: Source, size: Int): IntArray {
        val dataInputStream = inputStream.buffer()

        val bytes = dataInputStream.readByteArray(size * 4)
        val buffer = Buffer().apply { write(bytes) }
        val output = IntArray(size) { buffer.readInt() }

        return output
    }

    fun readInt(inputStream: Source): Int {
        val dataInputStream = inputStream.buffer()
        return dataInputStream.readInt()
    }

    fun BufferedSource.readByteArray(size: Int): ByteArray {
        val byteArray = ByteArray(size)
        this.readFully(byteArray)

        return byteArray
    }
}