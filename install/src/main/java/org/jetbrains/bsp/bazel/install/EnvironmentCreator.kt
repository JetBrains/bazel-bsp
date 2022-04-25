package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import io.vavr.control.Try
import java.nio.file.Path

open class EnvironmentCreator(
    projectRootDir: Path,
    private val discoveryDetails: BspConnectionDetails
) : BaseEnvironmentCreator(projectRootDir) {
    fun create(): Try<Void> = createDotBazelBsp().flatMap { createDotBsp(discoveryDetails) }
}
