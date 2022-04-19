package org.jetbrains.bsp.bazel.install.cli

import com.google.common.net.HostAndPort
import io.vavr.control.Try
import org.apache.commons.cli.*
import java.nio.file.Path
import java.nio.file.Paths

class CliOptionsProvider(private val args: Array<String>) {

    private val cliParserOptions: Options = Options()

    init {
        val helpOption = Option.builder(HELP_SHORT_OPT)
                .longOpt("help")
                .desc("Show help")
                .build()
        cliParserOptions.addOption(helpOption)

        val workspaceRootDirectoryOption = Option.builder(WORKSPACE_ROOT_DIR_SHORT_OPT)
                .longOpt("directory")
                .hasArg()
                .argName("path")
                .desc("Path to directory where bazel bsp server should be setup. "
                        + "Current directory will be used by default")
                .build()
        cliParserOptions.addOption(workspaceRootDirectoryOption)

        val projectViewFilePathOption = Option.builder(PROJECT_VIEW_FILE_PATH_SHORT_OPT)
                .longOpt("project_view_file")
                .hasArg()
                .argName("path")
                .desc("Path to project view file.")
                .build()
        cliParserOptions.addOption(projectViewFilePathOption)

        val targetsOption = Option.builder(TARGETS_SHORT_OPT)
                .longOpt("targets")
                .hasArgs()
                .argName("target")
                .desc("Project view targets, you can read more about it here:" +
                        " https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#targets.")
                .build()
        cliParserOptions.addOption(targetsOption)

        val buildFlagsOption = Option.builder(BUILD_FLAGS_SHORT_OPT)
                .longOpt("build_flags")
                .hasArgs()
                .argName("flag")
                .desc("Project view build flags, you can read more about it here:" +
                        " https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#build_flags")
                .build()
        cliParserOptions.addOption(buildFlagsOption)

        val bazelPathOption = Option.builder(BAZEL_PATH_SHORT_OPT)
                .longOpt("bazel_path")
                .hasArg()
                .argName("path")
                .desc("Project view bazel path, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#bazel_path")
                .build()
        cliParserOptions.addOption(bazelPathOption)

        val debuggerAddressOption = Option.builder(DEBUGGER_ADDRESS_SHORT_OPT)
                .longOpt("debugger_address")
                .hasArg()
                .argName("address")
                .desc("Project view debugger address, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#debugger_address")
                .build()
        cliParserOptions.addOption(debuggerAddressOption)

        val javaPathOption = Option.builder(JAVA_PATH_SHORT_OPT)
                .longOpt("java_path")
                .hasArg()
                .argName("path")
                .desc(
                        "Project view java path, you can read more about it here: " +
                                "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#java_path")
                .build()
        cliParserOptions.addOption(javaPathOption)
    }

    fun getOptions(): Try<CliOptions> {
        val parser = DefaultParser()
        return Try.of { parser.parse(cliParserOptions, args, false) }
                .map(::createCliOptions)
    }

    private fun createCliOptions(cmd: CommandLine): CliOptions =
            CliOptions(
                    helpCliOptions = createHelpCliOptions(cmd),
                    workspaceRootDir = workspaceRootDir(cmd),
                    projectViewFilePath = io.vavr.control.Option.of(projectViewFilePath(cmd)),
                    projectViewCliOptions = createProjectViewCliOptions(cmd),
            )

    private fun createHelpCliOptions(cmd: CommandLine): HelpCliOptions =
            HelpCliOptions(
                    isHelpOptionUsed = isHelpOptionUsed(cmd),
                    printHelp = ::printHelp,
            )

    private fun createProjectViewCliOptions(cmd: CommandLine): ProjectViewCliOptions =
            ProjectViewCliOptions(
                    javaPath = io.vavr.control.Option.of(javaPath(cmd)),
                    bazelPath = io.vavr.control.Option.of(bazelPath(cmd)),
                    debuggerAddress = io.vavr.control.Option.of(debuggerAddress(cmd)),
                    targets = io.vavr.control.Option.of(targets(cmd)),
                    buildFlags = io.vavr.control.Option.of(buildFlags(cmd)),
            )

    private fun isHelpOptionUsed(cmd: CommandLine): Boolean = cmd.hasOption(HELP_SHORT_OPT)

    private fun printHelp() {
        val formatter = HelpFormatter()
        formatter.width = 150
        formatter.printHelp(INSTALLER_BINARY_NAME, cliParserOptions)
    }

    private fun workspaceRootDir(cmd: CommandLine): Path =
            getOptionValueAndMapToPath(cmd, WORKSPACE_ROOT_DIR_SHORT_OPT) ?: calculateCurrentAbsoluteDirectory()

    private fun projectViewFilePath(cmd: CommandLine): Path? =
            getOptionValueAndMapToPath(cmd, PROJECT_VIEW_FILE_PATH_SHORT_OPT)
                    ?.let { calculateCurrentAbsoluteDirectory().resolve(it) }
                    ?.let(Path::normalize)

    private fun javaPath(cmd: CommandLine): Path? = getOptionValueAndMapToPath(cmd, JAVA_PATH_SHORT_OPT)
            ?.let { calculateCurrentAbsoluteDirectory().resolve(it) }
            ?.let(Path::normalize)

    private fun bazelPath(cmd: CommandLine): Path? = getOptionValueAndMapToPath(cmd, BAZEL_PATH_SHORT_OPT)
            ?.let { calculateCurrentAbsoluteDirectory().resolve(it) }
            ?.let(Path::normalize)

    private fun getOptionValueAndMapToPath(cmd: CommandLine, shortOpt: String): Path? =
            cmd.getOptionValue(shortOpt)?.let(Paths::get)

    private fun debuggerAddress(cmd: CommandLine): HostAndPort? =
            cmd.getOptionValue(DEBUGGER_ADDRESS_SHORT_OPT)?.let(HostAndPort::fromString)

    private fun targets(cmd: CommandLine): io.vavr.collection.List<String>? =
            cmd.getOptionValues(TARGETS_SHORT_OPT)
                    ?.toList()
                    ?.let { io.vavr.collection.List.ofAll(it) }

    private fun buildFlags(cmd: CommandLine): io.vavr.collection.List<String>? =
            cmd.getOptionValues(BUILD_FLAGS_SHORT_OPT)
                    ?.toList()
                    ?.let { io.vavr.collection.List.ofAll(it) }

    private fun calculateCurrentAbsoluteDirectory(): Path = Paths.get("").toAbsolutePath()

    companion object {
        private const val HELP_SHORT_OPT = "h"
        private const val WORKSPACE_ROOT_DIR_SHORT_OPT = "d"
        private const val PROJECT_VIEW_FILE_PATH_SHORT_OPT = "p"
        private const val TARGETS_SHORT_OPT = "t"
        private const val BUILD_FLAGS_SHORT_OPT = "f"
        private const val BAZEL_PATH_SHORT_OPT = "b"
        private const val DEBUGGER_ADDRESS_SHORT_OPT = "x"
        private const val JAVA_PATH_SHORT_OPT = "j"

        const val INSTALLER_BINARY_NAME = "bazelbsp-install"
    }
}
