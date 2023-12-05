package org.jetbrains.bsp.bazel.commons

import java.io.File

object FileUtils {
    fun getCacheDirectory(subfolder: String): File? {
        val path = System.getenv("XDG_CACHE_HOME") ?: run {
            val os = System.getProperty("os.name").lowercase()
            when {
              os.startsWith("windows") ->
                  System.getenv("LOCALAPPDATA") ?: System.getenv("APPDATA")
              os.startsWith("linux") ->
                  System.getenv("HOME") + "/.cache"
              os.startsWith("mac") ->
                  System.getenv("HOME") + "/Library/Caches"
              else -> return null
            }
        }
        val file = File(path, subfolder)
        try {
            file.mkdirs()
        } catch (e: Exception) {
            return null
        }
        if (!file.exists() || !file.isDirectory) {
            return null
        }
        return file
    }
}