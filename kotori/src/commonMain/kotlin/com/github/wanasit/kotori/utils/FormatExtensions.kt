package com.github.wanasit.kotori.utils

import net.sergeych.sprintf.format

fun Int.format(format: String="%,d") : String {
    return format.format(this)
}

fun Long.format(format: String="%,d") : String {
    return format.format(this)
}

fun Double.format(format: String="%.2f") : String {
    return format.format(this)
}