package org.jetbrains.bsp.bazel.installationcontext

import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import java.nio.file.Path
import java.nio.file.Paths


data class InstallationContextJavaPathEntity(override val value: Path) : ExecutionContextSingletonEntity<Path>()

object InstallationContextJavaPathEntityMapper {

    private const val JAVA_HOME_PROPERTY_KEY = "java.home"

    fun default(): InstallationContextJavaPathEntity = readFromSystemPropertyAndMapOrFailure()

    private fun readFromSystemPropertyAndMapOrFailure(): InstallationContextJavaPathEntity =
        readFromSystemPropertyAndMap()
            ?: error("System property '$JAVA_HOME_PROPERTY_KEY' is not specified! " +
                "Please install java and try to reinstall the server")


    private fun readFromSystemPropertyAndMap(): InstallationContextJavaPathEntity? =
        System.getProperty(JAVA_HOME_PROPERTY_KEY)
            ?.let(Paths::get)
            ?.let(::appendJavaBinary)
            ?.let(::map)

    private fun appendJavaBinary(javaHome: Path): Path =
        javaHome.resolve("bin/java")

    private fun map(rawJavaPath: Path): InstallationContextJavaPathEntity =
        InstallationContextJavaPathEntity(rawJavaPath)
}
