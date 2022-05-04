package org.jetbrains.bsp.bazel.installationcontext

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContextSingletonEntity
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapper
import org.jetbrains.bsp.bazel.executioncontext.api.ProjectViewToExecutionContextEntityMapperException
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection
import java.nio.file.Path
import java.nio.file.Paths

data class InstallationContextJavaPathEntity(override val value: Path) : ExecutionContextSingletonEntity<Path>()


internal object InstallationContextJavaPathEntityMapper :
    ProjectViewToExecutionContextEntityMapper<InstallationContextJavaPathEntity> {

    private const val NAME = "java path"
    private const val JAVA_HOME_PROPERTY_KEY = "java.home"

    override fun map(projectView: ProjectView): Try<InstallationContextJavaPathEntity> =
        fromProjectView(projectView) ?: readFromSystemPropertyAndMapOrFailure()

    private fun fromProjectView(projectView: ProjectView): Try<InstallationContextJavaPathEntity>? =
        projectView.javaPath?.let(::map)?.let{ Try.success(it) }

    private fun map(javaPathSection: ProjectViewJavaPathSection): InstallationContextJavaPathEntity =
        InstallationContextJavaPathEntity(javaPathSection.value)

    private fun readFromSystemPropertyAndMapOrFailure(): Try<InstallationContextJavaPathEntity> {
        return readFromSystemPropertyAndMap()?.let { Try.success(it) }
            ?: Try.failure(
                ProjectViewToExecutionContextEntityMapperException(
                    NAME,
                    "System property '$JAVA_HOME_PROPERTY_KEY' is not specified."
                )
            )
    }

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
