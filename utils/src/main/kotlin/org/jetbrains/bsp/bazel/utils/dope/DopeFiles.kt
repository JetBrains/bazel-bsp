package org.jetbrains.bsp.bazel.utils.dope

import io.vavr.control.Try
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

/**
 * Trys are dope, exceptions are not... Soooo use `DopeFiles` to perform actions on files
 * (it's even better than java `Files`)
 */
@Deprecated("dont use it")
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
            filePath.writeText(text)
        }

    // TODO we can do it in more kotlin way - https://youtrack.jetbrains.com/issue/BAZEL-58
    /**
     * Creates directories (recursive).
     *
     * @param dir - path of directory to create (including subdirectories)
     * @return
     *  - `Try.success` with dir path if operation was successful
     *  - `Try.failure` otherwise
     */
    fun createDirectories(dir: Path): Try<Path> =
        Try.of { Files.createDirectories(dir) }
}
