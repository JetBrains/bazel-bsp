package org.jetbrains.bsp.bazel.server.bsp.utils

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object FileUtils {
    /**
     * Important for aspect files, as writing the same content
     * updates filesystem's modification date and trigger Bazel's
     * "Checking cached action" step
     */
    fun Path.writeIfDifferent(fileContent: String) {
        if (!this.exists() || this.readText() != fileContent)
            this.writeText(fileContent)
    }
}