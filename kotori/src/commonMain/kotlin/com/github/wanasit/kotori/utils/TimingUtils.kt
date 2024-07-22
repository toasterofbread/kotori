package com.github.wanasit.kotori.utils

import kotlin.time.Duration
import kotlin.time.TimeSource

inline fun <Output> measureTimeWithOutput(block: () -> Output): Pair<Duration, Output> {
    val start = TimeSource.Monotonic.markNow()
    val output = block()
    return TimeSource.Monotonic.markNow() - start to output
}

inline fun <Output> runAndPrintTimeMillis(msg: String, block: () -> Output) : Output {
    val (time, output) = measureTimeWithOutput { block() }
    println("[$msg] took ${time.inWholeMilliseconds} ms")
    return output;
}