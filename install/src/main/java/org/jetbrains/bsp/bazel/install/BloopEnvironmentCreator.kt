package org.jetbrains.bsp.bazel.install

import com.google.gson.GsonBuilder
import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import java.nio.file.Path
import java.nio.file.Paths

class BloopEnvironmentCreator(
    private val cliOptions: CliOptions,
    private val installationContext: InstallationContext
    ) : EnvironmentCreator(cliOptions.workspaceRootDir) {

    private val projectRootDir = cliOptions.workspaceRootDir
    private val launcherArgumentCreator = LauncherArgumentCreator(installationContext)

    private companion object {
        data class WorkspaceSettings(
            val refreshProjectsCommand: List<String>
        )

        data class ProjectSettings(
            val targets: List<String>?,
            val projectName: String?
        )
    }

    override fun create(): Try<Void> = createDotBazelBsp()
        .flatMap { bazelBspPath ->
            createProjectSettings(bazelBspPath).flatMap {
                BloopBspConnectionDetailsCreator(bazelBspPath).create()
            }
        }
        .flatMap { createDotBsp(it) }
        .flatMap { createDotBloop() }

    private fun createProjectSettings(bazelBspPath: Path): Try<Void> {
        val projectSettings = ProjectSettings(
            cliOptions.projectViewCliOptions?.targets,
            cliOptions.projectName
        )
        val settingsFile = bazelBspPath.resolve("project.settings.json")
        return writeJsonToFile(settingsFile, projectSettings)
    }

    private fun createDotBloop(): Try<Void> =
        createDir(projectRootDir, ".bloop")
            .flatMap(::createBloopConfig)

    private fun createBloopConfig(path: Path): Try<Void> {
        return refreshProjectCmd().flatMap {
            val settings = WorkspaceSettings(it)
            val fileContent = GsonBuilder().setPrettyPrinting().create().toJson(settings)
            val bloopSettingsJson = path.resolve(Constants.BLOOP_SETTINGS_JSON_FILE_NAME)

            writeStringToFile(bloopSettingsJson, fileContent)
        }
    }

    private fun refreshProjectCmd(): Try<List<String>> {
        val pwd = Paths.get("").toAbsolutePath()
        return launcherArgumentCreator.classpathArgv().map {
            listOfNotNull(
                launcherArgumentCreator.javaBinaryArgv(),
                Constants.CLASSPATH_FLAG,
                it,
                launcherArgumentCreator.debuggerConnectionArgv(),
                Constants.BLOOP_BOOTSTRAP_CLASS_NAME,
                pwd.toString(),
                launcherArgumentCreator.projectViewFilePathArgv()
            )
        }
    }
}
