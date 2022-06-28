package org.jetbrains.bsp.bazel.install

import io.vavr.control.Try
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.install.cli.CliOptions
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext
import java.nio.file.Path
import java.nio.file.Paths

private data class WorkspaceSettings(
    val refreshProjectsCommand: List<String>
)

private data class ProjectSettings(
    val targets: List<String>?
)

class BloopEnvironmentCreator(
    private val cliOptions: CliOptions,
    private val installationContext: InstallationContext
) : EnvironmentCreator(cliOptions.workspaceRootDir) {

    private val projectRootDir = cliOptions.workspaceRootDir
    private val launcherArgumentCreator = LauncherArgumentCreator(installationContext)

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
            cliOptions.projectViewCliOptions?.targets
        )
        val settingsFile = bazelBspPath.resolve("project.settings.json")
        return writeJsonToFile(settingsFile, projectSettings)
    }

    private fun createDotBloop(): Try<Void> =
        createDir(projectRootDir, ".bloop")
            .flatMap(::createBloopConfig)

    private fun createBloopConfig(path: Path): Try<Void> =
        refreshProjectArgs().flatMap {
            val settings = WorkspaceSettings(it)
            val bloopSettingsJsonPath = path.resolve(Constants.BLOOP_SETTINGS_JSON_FILE_NAME)
            writeJsonToFile(bloopSettingsJsonPath, settings)
        }


    private fun refreshProjectArgs(): Try<List<String>> {
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
