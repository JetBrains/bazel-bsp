package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import org.jetbrains.bsp.bazel.commons.flatMap
import java.nio.file.Path

class BazelBspEnvironmentCreator(
    projectRootDir: Path,
    private val discoveryDetails: BspConnectionDetails
) : EnvironmentCreator(projectRootDir) {
    override fun create(): Result<Unit> = createDotBazelBsp().flatMap { createDotBsp(discoveryDetails) }
}
