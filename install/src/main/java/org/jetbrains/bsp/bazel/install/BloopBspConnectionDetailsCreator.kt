package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.Constants
import java.lang.IllegalArgumentException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

class BloopBspConnectionDetailsCreator(bazelBspPath: Path) {
    private val coursierDestination = bazelBspPath.resolve("cs")

    private fun downloadCoursier(): Try<Void> =
        if (Files.isRegularFile(coursierDestination) && Files.isExecutable(coursierDestination)) {
            Try.success(null)
        } else if (Files.exists(coursierDestination)) {
            Try.failure(IllegalArgumentException("file already exists: $coursierDestination, but was not executable"))
        } else {
            val url = System.getenv("FASTPASS_COURSIER_URL") ?: "https://git.io/coursier-cli"
            Try.run {
                Files.copy(
                    URL(url).openStream(),
                    coursierDestination
                )
                coursierDestination.toFile().setExecutable(true)
            }
        }

    fun create(): Try<BspConnectionDetails> =
        downloadCoursier().map {
            BspConnectionDetails(
                Constants.NAME,
                listOfNotNull(
                    coursierDestination.toString(),
                    "launch",
                    "ch.epfl.scala:bloop-launcher-core_2.13:1.5.0",
                    "--ttl",
                    "Inf",
                    "--",
                    "1.5.0"
                ),
                Constants.VERSION,
                Constants.BSP_VERSION,
                Constants.SUPPORTED_LANGUAGES
            )
        }

}
