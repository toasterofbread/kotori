package com.github.wanasit.kotori.dictionaries

import com.github.wanasit.kotori.Dictionary
import com.github.wanasit.kotori.dictionaries.utils.downloadIntoDirectory
import com.github.wanasit.kotori.dictionaries.utils.extractIntoDirectory
import com.github.wanasit.kotori.mecab.MeCabDictionary
import com.github.wanasit.kotori.mecab.MeCabTermFeatures
import java.io.File
import java.nio.charset.Charset

object Dictionaries {


    object Mecab {
        const val DEFAULT_DATA_DIR = "../data/mecab-dictionary/"

        @JvmStatic
        @JvmOverloads
        fun loadIpadic(dataDir: String = DEFAULT_DATA_DIR, charset: Charset = MeCabDictionary.DEFAULT_CHARSET): Dictionary<MeCabTermFeatures> {
            return MeCabDictionary.readFromDirectory(File(dataDir, MECAB_IPADIC_VERSION).path, charset)
        }

        @JvmStatic
        @JvmOverloads
        fun loadUnidic(dataDir: String = DEFAULT_DATA_DIR, charset: Charset = Charsets.UTF_8): Dictionary<MeCabTermFeatures> {
            return MeCabDictionary.readFromDirectory(File(dataDir, MECAB_UNIDIC_VERSION).path, charset)
        }

        private const val MECAB_IPADIC_VERSION = "mecab-ipadic-2.7.0-20070801"
        private const val MECAB_IPADIC_DOWNLOAD_URL = "https://atilika.com/releases/mecab-ipadic/${MECAB_IPADIC_VERSION}.tar.gz"

        private const val MECAB_UNIDIC_VERSION = "unidic-mecab-2.1.2_src"
        private const val MECAB_UNIDIC_DOWNLOAD_URL = "https://atilika.com/releases/unidic-mecab/${MECAB_UNIDIC_VERSION}.zip"

        fun downloadUniDic(
                dataDir: String = DEFAULT_DATA_DIR,
                overwrite: Boolean = false
        ) {
            val dataDirFile = File(dataDir)
            dataDirFile.mkdirs()

            if (File(dataDirFile, MECAB_UNIDIC_VERSION).exists() && !overwrite) {
                return
            }

            val downloadedFile = downloadIntoDirectory(dataDirFile, MECAB_UNIDIC_DOWNLOAD_URL)
            extractIntoDirectory(dataDirFile, downloadedFile)
            downloadedFile.delete()
        }

        fun downloadIPADic(
                dataDir: String = DEFAULT_DATA_DIR,
                overwrite: Boolean = false
        ) {
            val dataDirFile = File(dataDir)
            dataDirFile.mkdirs()

            if (File(dataDirFile, MECAB_IPADIC_VERSION).exists() && !overwrite) {
                return
            }

            val downloadedFile = downloadIntoDirectory(dataDirFile, MECAB_IPADIC_DOWNLOAD_URL)
            extractIntoDirectory(dataDirFile, downloadedFile)
            downloadedFile.delete()
        }
    }

    object Sudachi {
        const val DEFAULT_DATA_DIR = "../data/sudachi-dictionary/"

        fun smallDictionaryPath(dataDir: String = DEFAULT_DATA_DIR): String {
            return File(dataDir, SUDACHI_DICTIONARY_SMALL_FILE).path
        }

        fun coreDictionaryPath(dataDir: String = DEFAULT_DATA_DIR): String {
            return File(dataDir, SUDACHI_DICTIONARY_CORE_FILE).path
        }

        fun fullDictionaryPath(dataDir: String = DEFAULT_DATA_DIR): String {
            return File(dataDir, SUDACHI_DICTIONARY_FULL_FILE).path
        }

        private const val SUDACHI_DICTIONARY_VERSION = "sudachi-dictionary-20200330"

        private const val SUDACHI_DICTIONARY_SMALL_DOWNLOAD_URL = "https://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/${SUDACHI_DICTIONARY_VERSION}-small.zip"
        private const val SUDACHI_DICTIONARY_SMALL_FILE = "system_small.dic"

        private const val SUDACHI_DICTIONARY_CORE_DOWNLOAD_URL = "https://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/${SUDACHI_DICTIONARY_VERSION}-core.zip"
        private const val SUDACHI_DICTIONARY_CORE_FILE = "system_core.dic"

        private const val SUDACHI_DICTIONARY_FULL_DOWNLOAD_URL = "https://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/${SUDACHI_DICTIONARY_VERSION}-core.zip"
        private const val SUDACHI_DICTIONARY_FULL_FILE = "system_full.dic"

        fun downloadSmallDictionary(
                dataDir: String = DEFAULT_DATA_DIR,
                overwrite: Boolean = false
        ) {
            downloadDictionary(dataDir, overwrite,
                    SUDACHI_DICTIONARY_SMALL_DOWNLOAD_URL, SUDACHI_DICTIONARY_SMALL_FILE
            )
        }

        fun downloadCoreDictionary(
                dataDir: String = DEFAULT_DATA_DIR,
                overwrite: Boolean = false
        ) {
            downloadDictionary(dataDir, overwrite,
                    SUDACHI_DICTIONARY_CORE_DOWNLOAD_URL, SUDACHI_DICTIONARY_CORE_FILE
            )
        }

        fun downloadFullDictionary(
                dataDir: String = DEFAULT_DATA_DIR,
                overwrite: Boolean = false
        ) {
            downloadDictionary(dataDir, overwrite,
                    SUDACHI_DICTIONARY_FULL_DOWNLOAD_URL, SUDACHI_DICTIONARY_FULL_FILE
            )
        }


        private fun downloadDictionary(
                dataDir: String,
                overwrite: Boolean,
                url: String,
                expectedFile: String
        ) {
            val dataDirFile = File(dataDir)
            dataDirFile.mkdirs()

            if (File(dataDirFile, expectedFile).exists() && !overwrite) {
                return
            }

            val downloadedFile = downloadIntoDirectory(dataDirFile, url)
            extractIntoDirectory(dataDirFile, downloadedFile)

            File(dataDirFile, SUDACHI_DICTIONARY_VERSION).copyRecursively(dataDirFile)
            File(dataDirFile, SUDACHI_DICTIONARY_VERSION).deleteRecursively()
            downloadedFile.delete()
        }
    }
}