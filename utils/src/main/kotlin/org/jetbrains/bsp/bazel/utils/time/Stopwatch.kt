package org.jetbrains.bsp.bazel.utils.time

import java.time.Duration

class Stopwatch {

    private val start: Long

    init {
        start = now()
    }

    fun stop(): Duration = Duration.ofMillis(now() - start)

    private fun now(): Long = System.currentTimeMillis()

    companion object {
        fun start(): Stopwatch = Stopwatch()
    }
}
