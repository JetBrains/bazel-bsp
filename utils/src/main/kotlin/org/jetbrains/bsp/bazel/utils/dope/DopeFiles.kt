package org.jetbrains.bsp.bazel.utils.dope

import io.vavr.control.Try
import java.nio.file.Files
import java.nio.file.Path

/**
 * Trys are dope, exceptions are not... Soooo use `DopeFiles` to perform actions on files
 * (it's even better than java `Files`)
 */
object DopeFiles {

    // TODO we can do it in more kotlin way - https://youtrack.jetbrains.com/issue/BAZEL-58
    /**
     * Reads the text from the file.
     *
     * @param filePath - path to the file from which text should be read
     * @return
     *  - `Try.success` with text if operation was successful
     *  - `Try.failure` otherwise
     */
    fun readText(filePath: Path): Try<String> =
        Try.of { Files.readString(filePath) }

    // TODO we can do it in more kotlin way - https://youtrack.jetbrains.com/issue/BAZEL-58
    /**
     * Writes the text to the file, if file doesn't exist it creates it, including all subdirectories.
     *
     * @param filePath - path to the file where text should be written
     * @param text - text to write
     * @return
     *  - `Try.success` if operation was successful
     *  - `Try.failure` otherwise
     */
    fun writeText(filePath: Path, text: String): Try<Void> =
        Try.run {
            Files.createDirectories(filePath.parent)
            Files.writeString(filePath, text)
        }
}
