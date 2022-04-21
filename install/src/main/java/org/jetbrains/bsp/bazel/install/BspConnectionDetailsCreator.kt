package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import io.vavr.control.Option
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import java.nio.file.Path
import java.nio.file.Paths

class BspConnectionDetailsCreator(private val installationContext: InstallationContext) {

    fun create(): Try<BspConnectionDetails> =
            calculateArgv()
                    .map {
                        BspConnectionDetails(
                                Constants.NAME,
                                it,
                                Constants.VERSION,
                                Constants.BSP_VERSION,
                                Constants.SUPPORTED_LANGUAGES)
                    }

    private fun calculateArgv(): Try<List<String>> =
            classpathArgv().map {
                listOfNotNull(
                        javaBinaryArgv(),
                        CLASSPATH_FLAG,
                        it,
                        debuggerConnectionArgv(),
                        SERVER_CLASS_NAME,
                        projectViewFilePathArgv(),
                )
            }

    private fun javaBinaryArgv(): String = installationContext.javaPath.value.toString()

    private fun classpathArgv(): Try<String> =
            readSystemProperty("java.class.path").map(::mapClasspathToAbsolutePaths)

    private fun readSystemProperty(name: String): Try<String> =
            Option.of(System.getProperty(name))
                    .toTry { NoSuchElementException("Could not read $name system property") }

    private fun mapClasspathToAbsolutePaths(systemPropertyClasspath: String): String =
            systemPropertyClasspath.split(":")
                    .map(Paths::get)
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .joinToString(separator = ":", transform = Path::toString)

    private fun debuggerConnectionArgv(): String? =
            installationContext
                    .debuggerAddress.orNull
                    ?.value
                    ?.let { "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$it" }

    private fun projectViewFilePathArgv(): String? = installationContext.projectViewFilePath.orNull?.toString()

    private companion object {
        private const val CLASSPATH_FLAG = "-classpath"
        private const val SERVER_CLASS_NAME = "org.jetbrains.bsp.bazel.server.ServerInitializer"
    }
}
