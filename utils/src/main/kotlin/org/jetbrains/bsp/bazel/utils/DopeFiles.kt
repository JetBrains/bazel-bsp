package org.jetbrains.bsp.bazel.utils

import io.vavr.control.Try
import java.nio.file.Files
import java.nio.file.Path

object DopeFiles {

    // TODO we can do it in more kotlin way - https://youtrack.jetbrains.com/issue/BAZEL-58
    fun readText(filePath: Path): Try<String> =
        Try.of { Files.readString(filePath) }
}
