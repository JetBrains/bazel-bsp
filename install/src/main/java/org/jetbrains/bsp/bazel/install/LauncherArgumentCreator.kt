package org.jetbrains.bsp.bazel.install

import io.vavr.control.Option
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class LauncherArgumentCreator(private val installationContext: InstallationContext) {
    fun javaBinaryArgv(): String = installationContext.javaPath.value.toString()

    fun classpathArgv(): Try<String> =
        readSystemProperty("java.class.path").map(::mapClasspathToAbsolutePaths)

    private fun readSystemProperty(name: String): Try<String> =
        Option.of(System.getProperty(name))
            .toTry { NoSuchElementException("Could not read $name system property") }

    private fun mapClasspathToAbsolutePaths(systemPropertyClasspath: String): String =
        systemPropertyClasspath.split(File.pathSeparator)
            .map(Paths::get)
            .map(Path::toAbsolutePath)
            .map(Path::normalize)
            .joinToString(separator = File.pathSeparator, transform = Path::toString)

    fun debuggerConnectionArgv(): String? =
        installationContext
            .debuggerAddress
            ?.value
            ?.let { "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=$it" }

    fun projectViewFilePathArgv(): String? = installationContext.projectViewFilePath?.toString()

    fun bazelWorkspaceRootDir(): String = installationContext.bazelWorkspaceRootDir.toString()

}
