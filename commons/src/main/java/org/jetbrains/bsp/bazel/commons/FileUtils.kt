package org.jetbrains.bsp.bazel.commons

import org.apache.logging.log4j.LogManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


val log = LogManager.getLogger(FileUtils::class.java)

object FileUtils {
    fun getCacheDirectory(subfolder: String): File? {
        val path = System.getenv("XDG_CACHE_HOME") ?: run {
            val os = System.getProperty("os.name").lowercase()
            if (os.startsWith("windows")) {
                System.getenv("LOCALAPPDATA") ?: System.getenv("APPDATA")
            } else if (os.startsWith("linux")) {
                System.getenv("HOME") + "/.cache"
            } else if (os.startsWith("mac")) {
                System.getenv("HOME") + "/Library/Caches"
            } else {
                null
            }
        } ?: return null
        val file = File(path, subfolder)
        try {
            file.mkdirs()
        } catch (e: Exception) {
            return null
        }
        if (!file.exists() || !file.isDirectory()) {
            return null
        }
        return file
    }

    // TODO: Could be async, but for now it's used only in a sync method
    fun downloadFile(url: URL, file: File) {
        url.openStream().use { inp ->
            BufferedInputStream(inp).use { bis ->
                FileOutputStream(file).use { fos ->
                    val data = ByteArray(65536)
                    var count: Int
                    var size: Long = 0
                    var lastLogTime = System.currentTimeMillis()
                    while (bis.read(data, 0, 65536).also { count = it } != -1) {
                        fos.write(data, 0, count)
                        size += count
                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        if (System.currentTimeMillis() - lastLogTime > 1000) {
                            log.info("Downloading ${file.name}: ${size / 1024} KB")
                            lastLogTime = System.currentTimeMillis()
                        }
                    }
                    log.info("Finished downloading ${file.name}")
                }
            }
        }
    }
}