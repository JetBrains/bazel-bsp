package org.jetbrains.bsp.bazel.install

import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class LauncherArgumentCreator(private val installationContext: InstallationContext) {
    fun javaBinaryArgv(): String = installationContext.javaPath.value.toString()

    fun classpathArgv(): String =
        mapClasspathToAbsolutePaths(readSystemProperty("java.class.path"))

    private fun readSystemProperty(name: String): String =
        System.getProperty(name) ?: throw NoSuchElementException("Could not read $name system property")

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

    fun projectViewFilePathArgv(): String = installationContext.projectViewFilePath.toString()

    fun bazelWorkspaceRootDir(): String = installationContext.bazelWorkspaceRootDir.toString()

}
