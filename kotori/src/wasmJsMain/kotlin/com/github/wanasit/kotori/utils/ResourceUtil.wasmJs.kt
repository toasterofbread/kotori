package com.github.wanasit.kotori.utils

import okio.Source

actual object ResourceUtil {
    /**
     * Read Java resource as a stream.
     * Also automatically detect and handle `.gz` extension
     */
    actual fun readResourceAsStream(
        namespace: String,
        filename: String
    ): Source? = null
}
