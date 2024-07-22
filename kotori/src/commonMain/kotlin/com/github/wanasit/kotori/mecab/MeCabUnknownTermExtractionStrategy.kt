package com.github.wanasit.kotori.mecab

import com.github.wanasit.kotori.optimized.tries.getChars
import com.github.wanasit.kotori.optimized.unknown.CharCategory
import com.github.wanasit.kotori.optimized.unknown.CharCategoryDefinition
import com.github.wanasit.kotori.optimized.unknown.UnknownTermExtractionByCharacterCategory
import com.github.wanasit.kotori.utils.IOUtils
import io.ktor.utils.io.charsets.Charset
import okio.FileSystem
import okio.Path
import okio.Source

object MeCabUnknownTermExtractionStrategy {

    fun readFromDirectory(
            fileSystem: FileSystem, dir: Path, charset: Charset = MeCabDictionary.DEFAULT_CHARSET
    ) : UnknownTermExtractionByCharacterCategory<MeCabTermFeatures> = readFromFileInputStreams(
            fileSystem.source(dir.resolve(MeCabDictionary.FILE_NAME_UNKNOWN_ENTRIES)),
            fileSystem.source(dir.resolve(MeCabDictionary.FILE_NAME_CHARACTER_DEFINITION)),
            charset)

    fun readFromFileInputStreams(
            unknownDefinitionInputStream: Source,
            charDefinitionInputStream: Source,
            charset: Charset
    ) : UnknownTermExtractionByCharacterCategory<MeCabTermFeatures> {
        val unknownTermEntries = MeCabTermFeatures.readTermEntriesFromFileInputStream(unknownDefinitionInputStream, charset)
        val charDefinitionLookup = MeCabCharDefinition
                .readFromCharDefinitionFileInputStream(charDefinitionInputStream, charset)
        return create(charDefinitionLookup, unknownTermEntries)
    }

    fun readFromByteArrays(
        unknownDefinitionByteArray: ByteArray,
        charDefinitionByteArray: ByteArray,
        charset: Charset
    ) : UnknownTermExtractionByCharacterCategory<MeCabTermFeatures> {
        val unknownTermEntries = MeCabTermFeatures.readTermEntriesFromByteArray(unknownDefinitionByteArray, charset)
        val charDefinitionLookup = MeCabCharDefinition
            .readFromCharDefinitionByteArray(charDefinitionByteArray, charset)
        return create(charDefinitionLookup, unknownTermEntries)
    }

    fun create(
            charDefinition: MeCabCharDefinition,
            unknownTermEntries: List<MeCabTermEntry>
    ): UnknownTermExtractionByCharacterCategory<MeCabTermFeatures> {

        val categoryNameLookup = charDefinition.createCategoryNameLookup()
        val charToCategories = charDefinition.createCharToCategoryMapping(categoryNameLookup)
        val definitionMapping = charDefinition.createCategoryToDefinition()

        val termEntryMapping = unknownTermEntries.groupBy { it.surfaceForm }
                .mapKeys { categoryNameLookup[it.key]
                        ?: throw IllegalArgumentException("Unknown category name '${it.key}'") }

        return UnknownTermExtractionByCharacterCategory.fromUnoptimizedMapping(
                charToCategories, definitionMapping, termEntryMapping)
    }
}

/**
 * This class represent the character definition as defined in char.def file
 */
class MeCabCharDefinition constructor(
        val categoryDefinitions: List<MecabCharCategoryDefinition>,
        val categoryCharCodeRanges: List<Triple<String, Int, Int>>
) {
    /** char.def example
    # This is comment
    ...
    DEFAULT         0 1 0  # DEFAULT is a mandatory category!
    SPACE           0 1 0
    ...
    0xFF10..0xFF19 NUMERIC
    ...

    0x3007 SYMBOL KANJINUMERIC
     **/
    data class MecabCharCategoryDefinition(
            val categoryName: String,
            val invoke: Boolean,
            val group: Boolean,
            val length: Short
    ) {
        fun toCharCategoryDefinition() : CharCategoryDefinition {
            return CharCategoryDefinition(invoke, group, length)
        }
    }

    companion object {

        fun readFromCharDefinitionFileInputStream(inputStream: Source, charset: Charset) : MeCabCharDefinition {
            return readFromLines(IOUtils.readStringWithCharset(inputStream, charset).split('\n').filter { it.isNotBlank() })
        }

        fun readFromCharDefinitionByteArray(byteArray: ByteArray, charset: Charset) : MeCabCharDefinition {
            return readFromLines(IOUtils.readStringWithCharset(byteArray, charset).split('\n').filter { it.isNotBlank() })
        }

        fun readFromLines(lines: List<String>) : MeCabCharDefinition {
            val commentRegEx = "\\s*#.*".toRegex();

            val categoryDefinitions: MutableList<MecabCharCategoryDefinition> = mutableListOf()
            val mappingEntries: MutableList<Triple<String, Int, Int>> = mutableListOf()

            lines
                    .map { commentRegEx.replace(it, "").trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { line: String ->

                        if (line.startsWith("0x")) {
                            mappingEntries.addAll(parseMapping(line))

                        } else {
                            val definition = parseCategory(line)
                            if (definition.categoryName == "DEFAULT") {
                                categoryDefinitions.add(0, definition)
                            } else {
                                categoryDefinitions.add(definition);
                            }
                        }

                        line.getChars()
                    }

            return MeCabCharDefinition(categoryDefinitions, mappingEntries);
        }

        private fun parseCategory(input: String): MecabCharCategoryDefinition {

            val whiteSpaceRegEx = "\\s+".toRegex();
            val values = whiteSpaceRegEx.split(input)

            val classname = values[0]
            val invoke = values[1].toInt() == 1
            val group = values[2].toInt() == 1
            val length = values[3].toShort()

            return MecabCharCategoryDefinition(classname, invoke, group, length);
        }

        private fun parseMapping(input: String) : Iterable<Triple<String, Int, Int>> {

            val whiteSpaceRegEx = "\\s+".toRegex();
            val rangeSymbolRegex = "\\.\\.".toRegex();

            val values = whiteSpaceRegEx.split(input)
            check(values.size >= 2)
            val codepointParts = rangeSymbolRegex.split(values[0]);
            val categories: List<String> = values.drop(1);

            val lowerCodepoint: Int;
            val upperCodepoint: Int;
            if (codepointParts.size == 2) {
                lowerCodepoint = codepointParts[0].fromHex()
                upperCodepoint = codepointParts[1].fromHex()
            } else {
                lowerCodepoint = codepointParts[0].fromHex()
                upperCodepoint = codepointParts[0].fromHex()
            }

            return categories.map { Triple(it, lowerCodepoint, upperCodepoint) }
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun String.fromHex(): Int =
            removePrefix("0x").hexToInt()
    }

    fun createCategoryNameLookup() : Map<String, CharCategory> {
        return categoryDefinitions.withIndex().associate { it.value.categoryName to it.index }
    }

    fun createCharToCategoryMapping(
            categoryNameLookup: Map<String, CharCategory> = createCategoryNameLookup()
    ): Map<Char, List<CharCategory>> {

        val tmpArray: Array<MutableList<Int>?> = arrayOfNulls(0xffff + 1)
        categoryCharCodeRanges.forEach {
            val charCategory = categoryNameLookup[it.first]
            if (charCategory != null) {
                for (i in it.second..it.third) {

                    if (tmpArray[i] == null) {
                        tmpArray[i] = mutableListOf(charCategory)
                    } else {
                        tmpArray[i]?.add(charCategory)
                    }
                }
            }
        }

        return tmpArray.mapIndexed {
            charCode, categories-> charCode.toChar() to (categories ?: listOf(0))
        }.toMap()
    }

    fun createCategoryToDefinition(): Map<CharCategory, CharCategoryDefinition> {
        return categoryDefinitions
                .mapIndexed { i : CharCategory, definition ->  i to definition.toCharCategoryDefinition() }
                .toMap()
    }
}
