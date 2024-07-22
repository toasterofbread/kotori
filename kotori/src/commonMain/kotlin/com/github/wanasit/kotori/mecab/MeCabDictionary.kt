package com.github.wanasit.kotori.mecab

import com.github.wanasit.kotori.ConnectionCost
import com.github.wanasit.kotori.Dictionary
import com.github.wanasit.kotori.TermDictionary
import com.github.wanasit.kotori.optimized.PlainConnectionCostTable
import com.github.wanasit.kotori.optimized.PlainTermDictionary
import com.github.wanasit.kotori.utils.IOUtils
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.forName
import okio.FileSystem
import okio.Path
import okio.Source

/**
 *
 */
object MeCabDictionary {

    const val FILE_NAME_CONNECTION_COST = "matrix.def"
    const val FILE_NAME_UNKNOWN_ENTRIES = "unk.def"
    const val FILE_NAME_CHARACTER_DEFINITION = "char.def"

    val DEFAULT_CHARSET: Charset = Charsets.forName("EUC-JP")

    fun readFromDirectory(
            fileSystem: FileSystem,
            dir: Path,
            charset: Charset = DEFAULT_CHARSET
    ) : Dictionary<MeCabTermFeatures> {

        check(fileSystem.exists(dir))

        val termDictionary = MeCabTermDictionary.readFromDirectory(fileSystem, dir, charset)
        val termConnection = MeCabConnectionCost.readFromInputStream(
                fileSystem.source(dir.resolve(FILE_NAME_CONNECTION_COST)),
                charset = DEFAULT_CHARSET)

        val unknownTermDictionary = MeCabUnknownTermExtractionStrategy.readFromDirectory(fileSystem, dir, charset)

        return Dictionary(
                termDictionary,
                termConnection,
                unknownTermDictionary
        )
    }
}

object MeCabTermDictionary {

    fun readFromDirectory(
            fileSystem: FileSystem,
            dir: Path,
            charset: Charset
    ) : TermDictionary<MeCabTermFeatures> {
        val dictionaryEntries = fileSystem.list(dir)
                .filter { it.name.endsWith("csv") }
                .sortedBy { it.name }
                .flatMap { MeCabTermFeatures.readTermEntriesFromFileInputStream(fileSystem.source(it), charset=charset) }

        return PlainTermDictionary(dictionaryEntries.toTypedArray())
    }

    fun readFromInputStream(inputStream: Source, charset: Charset) : TermDictionary<MeCabTermFeatures> {
        val dictionaryEntries = MeCabTermFeatures.readTermEntriesFromFileInputStream(inputStream, charset)
        return PlainTermDictionary(dictionaryEntries.toTypedArray())
    }
}

object MeCabConnectionCost {

    fun readFromInputStream(inputStream: Source, charset: Charset) : ConnectionCost {
        val lines = IOUtils.readStringWithCharset(inputStream, charset).split('\n').filter { it.isNotBlank() }
        return readFromInputStream(lines)
    }

    fun readFromByteArray(byteArray: ByteArray, charset: Charset) : ConnectionCost {
        val lines = IOUtils.readStringWithCharset(byteArray, charset).split('\n').filter { it.isNotBlank() }
        return readFromInputStream(lines)
    }

    private fun readFromInputStream(lines: List<String>) : ConnectionCost {
        val whiteSpaceRegEx = "\\s+".toRegex()

        val cardinality = whiteSpaceRegEx.split(lines.get(0))
        val fromIdCardinality = cardinality[0].toInt()
        val toIdCardinality = cardinality[1].toInt()
        val array = PlainConnectionCostTable(fromIdCardinality, toIdCardinality)

        lines.drop(1)
                .forEach {

                    val values = whiteSpaceRegEx.split(it)

                    val fromId = values[0].toInt()
                    val toId = values[1].toInt()
                    val cost = values[2].toInt()
                    array.put(fromId, toId, cost)
                }

        return array
    }
}