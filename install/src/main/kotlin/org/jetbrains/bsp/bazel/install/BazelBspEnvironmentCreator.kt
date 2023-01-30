package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import io.vavr.control.Try
import java.nio.file.Path

class BazelBspEnvironmentCreator(
    projectRootDir: Path,
    private val discoveryDetails: BspConnectionDetails
) : EnvironmentCreator(projectRootDir) {
    override fun create(): Try<Void> = createDotBazelBsp().flatMap { createDotBsp(discoveryDetails) }
}
