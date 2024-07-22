package com.github.wanasit.kotori.utils

import okio.Source

expect object ResourceUtil {

    /**
     * Read Java resource as a stream.
     * Also automatically detect and handle `.gz` extension
     */
    fun readResourceAsStream(namespace: String, filename: String): Source?
}
