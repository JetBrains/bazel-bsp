package org.jetbrains.bsp.bazel.server.sync

import java.util.concurrent.ConcurrentHashMap

class MetricsLogger(val logMemory: Boolean) {

    private val logs: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    fun log(key: String, value: Long) {
        logs[key] = value
    }

    fun logMemory(key: String) {
        if(logMemory) {
            System.gc()
            val usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024
            log(key, usedMemory)
        }
    }

    fun dump(): String {
        return logs.asIterable().joinToString("\n") { (k, v) -> "$k $v" }
    }

}
