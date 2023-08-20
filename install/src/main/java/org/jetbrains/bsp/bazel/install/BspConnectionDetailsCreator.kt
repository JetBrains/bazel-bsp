package org.jetbrains.bsp.bazel.install

import ch.epfl.scala.bsp4j.BspConnectionDetails
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext

class BspConnectionDetailsCreator(installationContext: InstallationContext, private val produceTraceLog: Boolean) {
    private val launcherArgumentCreator = LauncherArgumentCreator(installationContext)

    fun create(): Result<BspConnectionDetails> =
        calculateArgv()
            .map {
                BspConnectionDetails(
                    Constants.NAME,
                    it,
                    Constants.VERSION,
                    Constants.BSP_VERSION,
                    Constants.SUPPORTED_LANGUAGES
                )
            }

    private fun calculateArgv(): Result<List<String>> =
        launcherArgumentCreator.classpathArgv().map {
            listOfNotNull(
                launcherArgumentCreator.javaBinaryArgv(),
                Constants.CLASSPATH_FLAG,
                it,
                launcherArgumentCreator.debuggerConnectionArgv(),
                Constants.SERVER_CLASS_NAME,
                launcherArgumentCreator.bazelWorkspaceRootDir(),
                launcherArgumentCreator.projectViewFilePathArgv(),
                produceTraceLog.toString(),
            )
        }

}
