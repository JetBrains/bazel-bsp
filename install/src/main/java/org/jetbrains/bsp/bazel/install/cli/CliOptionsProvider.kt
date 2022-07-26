package org.jetbrains.bsp.bazel.install.cli

import io.vavr.control.Try
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.nio.file.Path
import java.nio.file.Paths

class CliOptionsProvider(private val args: Array<String>) {

    private val cliParserOptions: Options = Options()

    init {
        val helpOption = Option.builder(HELP_SHORT_OPT)
            .longOpt("help")
            .desc("Show help.")
            .build()
        cliParserOptions.addOption(helpOption)

        val workspaceRootDirectoryOption = Option.builder(WORKSPACE_ROOT_DIR_SHORT_OPT)
            .longOpt("directory")
            .hasArg()
            .argName("path")
            .desc(
                "Path to directory where bazel bsp server should be setup. "
                        + "Current directory will be used by default."
            )
            .build()
        cliParserOptions.addOption(workspaceRootDirectoryOption)

        val projectViewFilePathOption = Option.builder(PROJECT_VIEW_FILE_PATH_SHORT_OPT)
            .longOpt("project_view_file")
            .hasArg()
            .argName("path")
            .desc(
                "Path to project view file. " +
                        "OR The path of the new project view file which will be generated using generation flags."
            )
            .build()
        cliParserOptions.addOption(projectViewFilePathOption)

        val targetsOption = Option.builder(TARGETS_SHORT_OPT)
            .longOpt("targets")
            .hasArgs()
            .argName("targets")
            .desc(
                "Add targets to the generated project view file, you can read more about it here:" +
                        " https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#targets."
            )
            .build()
        cliParserOptions.addOption(targetsOption)

        val excludedTargetsOption = Option.builder()
            .longOpt(EXCLUDED_TARGETS_LONG_OPT)
            .hasArgs()
            .argName("excluded targets")
            .desc(
                "Add excluded targets to the generated project view file, you can read more about it here:" +
                        " https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#targets."
            )
            .build()
        cliParserOptions.addOption(excludedTargetsOption)

        val buildFlagsOption = Option.builder(BUILD_FLAGS_SHORT_OPT)
            .longOpt("build_flags")
            .hasArgs()
            .argName("flags")
            .desc(
                "Add build flags to the generated project view file, you can read more about it here:" +
                        " https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#build_flags."
            )
            .build()
        cliParserOptions.addOption(buildFlagsOption)

        val bazelPathOption = Option.builder(BAZEL_PATH_SHORT_OPT)
            .longOpt("bazel_path")
            .hasArg()
            .argName("path")
            .desc(
                "Add bazel path to the generated project view file, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#bazel_path."
            )
            .build()
        cliParserOptions.addOption(bazelPathOption)

        val debuggerAddressOption = Option.builder(DEBUGGER_ADDRESS_SHORT_OPT)
            .longOpt("debugger_address")
            .hasArg()
            .argName("address")
            .desc(
                "Add debugger address to the generated project view file, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#debugger_address."
            )
            .build()
        cliParserOptions.addOption(debuggerAddressOption)

        val javaPathOption = Option.builder(JAVA_PATH_SHORT_OPT)
            .longOpt("java_path")
            .hasArg()
            .argName("path")
            .desc(
                "Add java path to the generated project view file, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#java_path."
            )
            .build()
        cliParserOptions.addOption(javaPathOption)

        val manualTargetsOption = Option.builder(BUILD_MANUAL_TARGETS_OPT)
            .longOpt("build_manual_targets")
            .desc(
                "Add build manual target to the generated project view file, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#build_manual_targets."
            )
            .build()
        cliParserOptions.addOption(manualTargetsOption)


        val directoriesOption = Option.builder(DIRECTORIES_SHORT_OPT)
            .longOpt("directories")
            .hasArgs()
            .argName("directories")
            .desc(
                "Add directories to the generated project view file, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#directories."
            )
            .build()
        cliParserOptions.addOption(directoriesOption)

        val excludedDirectoriesOption = Option.builder()
            .longOpt(EXCLUDED_DIRECTORIES_LONG_OPT)
            .hasArgs()
            .argName("excluded directories")
            .desc(
                "Add excluded directories to the generated project view file, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#directories."
            )
            .build()
        cliParserOptions.addOption(excludedDirectoriesOption)

        val deriveTargetsFromDirectoriesOption = Option.builder(DERIVE_TARGETS_FLAG_SHORT_OPT)
            .longOpt("derive_targets_from_directories")
            .desc(
                "Add derive_targets_from_directories to the generated project view file, you can read more about it here: " +
                        "https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#derive_targets_from_directories."
            )
            .build()
        cliParserOptions.addOption(deriveTargetsFromDirectoriesOption)

        val importDepthOption = Option.builder(IMPORT_DEPTH_SHORT_OPT)
            .longOpt("import_depth")
            .hasArg()
            .argName("value")
            .desc(
                "Add import depth to to the generated project view file, you can read more about it here:" +
                        " https://github.com/JetBrains/bazel-bsp/tree/master/executioncontext/projectview#import_depth."
            )
            .build()
        cliParserOptions.addOption(importDepthOption)

        val useBloopOption = Option.builder(USE_BLOOP_SHORT_OPT)
            .longOpt("use_bloop")
            .desc("Use bloop as the BSP server rather than bazel-bsp.")
            .build()
        cliParserOptions.addOption(useBloopOption)

        val bazelWorkspaceRootDirOption = Option.builder(BAZEL_WORKSPACE_ROOT_DIR_OPT)
            .longOpt("bazel_workspace")
            .hasArg()
            .desc("Add path to Bazel project's root directory. By default, it is the same as -$BAZEL_PATH_SHORT_OPT")
            .build()
        cliParserOptions.addOption(bazelWorkspaceRootDirOption)
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
            projectViewFilePath = projectViewFilePath(cmd),
            projectViewCliOptions = createProjectViewCliOptions(cmd),
            bloopCliOptions = createBloopCliOptions(cmd),
            bazelWorkspaceRootDir = bazelWorkspaceRootDir(cmd),
        )

    private fun workspaceRootDir(cmd: CommandLine): Path =
        getOptionValueAndMapToAbsolutePath(cmd, WORKSPACE_ROOT_DIR_SHORT_OPT) ?: calculateCurrentAbsoluteDirectory()

    private fun projectViewFilePath(cmd: CommandLine): Path? =
        getOptionValueAndMapToAbsolutePath(cmd, PROJECT_VIEW_FILE_PATH_SHORT_OPT)

    private fun createHelpCliOptions(cmd: CommandLine): HelpCliOptions =
        HelpCliOptions(
            isHelpOptionUsed = isHelpOptionUsed(cmd),
            printHelp = ::printHelp,
        )

    private fun bazelWorkspaceRootDir(cmd: CommandLine): Path =
        getOptionValueAndMapToAbsolutePath(cmd, BAZEL_WORKSPACE_ROOT_DIR_OPT) ?: workspaceRootDir(cmd)

    private fun isHelpOptionUsed(cmd: CommandLine): Boolean = cmd.hasOption(HELP_SHORT_OPT)

    private fun useBloop(cmd: CommandLine): Boolean = cmd.hasOption(USE_BLOOP_SHORT_OPT)

    private fun printHelp() {
        val formatter = HelpFormatter()
        formatter.width = 160
        formatter.printHelp(
            INSTALLER_BINARY_NAME,
            null,
            cliParserOptions,
            "If any generation flag (-b, -f, -j, -t, -x,-m ,-r, -v, -i) " +
                    "is used, the installer will generate a new project view file with these sections. " +
                    "If --project_view_file (-p) flag is used as well, the new project view file " +
                    "will be created under this location (it will override the existing file if exists). " +
                    "Otherwise the new file `projectview.bazelproject` will be created.",
            true
        )
    }

    private fun createBloopCliOptions(cmd: CommandLine): BloopCliOptions =
        BloopCliOptions(useBloop = useBloop(cmd))

    private fun createProjectViewCliOptions(cmd: CommandLine): ProjectViewCliOptions? =
        if (isAnyGenerationFlagSet(cmd))
            ProjectViewCliOptions(
                javaPath = javaPath(cmd),
                bazelPath = bazelPath(cmd),
                debuggerAddress = debuggerAddress(cmd),
                targets = targets(cmd),
                excludedTargets = excludedTargets(cmd),
                buildFlags = buildFlags(cmd),
                buildManualTargets = buildManualTargets(cmd),
                directories = directories(cmd),
                excludedDirectories = excludedDirectories(cmd),
                deriveTargetsFromDirectories = deriveTargetsFlag(cmd),
                importDepth = importDepth(cmd),
            )
        else null

    private fun isAnyGenerationFlagSet(cmd: CommandLine): Boolean =
        cmd.hasOption(TARGETS_SHORT_OPT) or
                cmd.hasOption(EXCLUDED_TARGETS_LONG_OPT) or
                cmd.hasOption(JAVA_PATH_SHORT_OPT) or
                cmd.hasOption(BAZEL_PATH_SHORT_OPT) or
                cmd.hasOption(DEBUGGER_ADDRESS_SHORT_OPT) or
                cmd.hasOption(BUILD_FLAGS_SHORT_OPT) or
                cmd.hasOption(BUILD_MANUAL_TARGETS_OPT) or
                cmd.hasOption(BUILD_FLAGS_SHORT_OPT) or
                cmd.hasOption(DIRECTORIES_SHORT_OPT) or
                cmd.hasOption(EXCLUDED_DIRECTORIES_LONG_OPT) or
                cmd.hasOption(DERIVE_TARGETS_FLAG_SHORT_OPT) or
                cmd.hasOption(IMPORT_DEPTH_SHORT_OPT)

    private fun javaPath(cmd: CommandLine): Path? = getOptionValueAndMapToAbsolutePath(cmd, JAVA_PATH_SHORT_OPT)

    private fun bazelPath(cmd: CommandLine): Path? = getOptionValueAndMapToAbsolutePath(cmd, BAZEL_PATH_SHORT_OPT)

    private fun getOptionValueAndMapToAbsolutePath(cmd: CommandLine, shortOpt: String): Path? =
        cmd.getOptionValue(shortOpt)?.let(Paths::get)
            ?.let { calculateCurrentAbsoluteDirectory().resolve(it) }
            ?.let(Path::normalize)

    private fun debuggerAddress(cmd: CommandLine): String? = cmd.getOptionValue(DEBUGGER_ADDRESS_SHORT_OPT)

    private fun buildManualTargets(cmd: CommandLine): Boolean = cmd.hasOption(BUILD_MANUAL_TARGETS_OPT)

    private fun importDepth(cmd: CommandLine): Int? = cmd.getOptionValue(IMPORT_DEPTH_SHORT_OPT)?.toInt()

    private fun targets(cmd: CommandLine): List<String>? = cmd.getOptionValues(TARGETS_SHORT_OPT)?.toList()

    private fun excludedTargets(cmd: CommandLine): List<String>? = cmd.getOptionValues(EXCLUDED_TARGETS_LONG_OPT)?.toList()

    private fun buildFlags(cmd: CommandLine): List<String>? = cmd.getOptionValues(BUILD_FLAGS_SHORT_OPT)?.toList()

    private fun calculateCurrentAbsoluteDirectory(): Path = Paths.get("").toAbsolutePath()

    private fun directories(cmd: CommandLine): List<String>? = cmd.getOptionValues(DIRECTORIES_SHORT_OPT)?.toList()

    private fun excludedDirectories(cmd: CommandLine): List<String>? = cmd.getOptionValues(EXCLUDED_DIRECTORIES_LONG_OPT)?.toList()

    private fun deriveTargetsFlag(cmd: CommandLine): Boolean = cmd.hasOption(DERIVE_TARGETS_FLAG_SHORT_OPT)

    companion object {
        private const val HELP_SHORT_OPT = "h"
        private const val WORKSPACE_ROOT_DIR_SHORT_OPT = "d"
        private const val PROJECT_VIEW_FILE_PATH_SHORT_OPT = "p"
        private const val TARGETS_SHORT_OPT = "t"
        private const val EXCLUDED_TARGETS_LONG_OPT = "excluded-targets"
        private const val BUILD_FLAGS_SHORT_OPT = "f"
        private const val BAZEL_PATH_SHORT_OPT = "b"
        private const val DEBUGGER_ADDRESS_SHORT_OPT = "x"
        private const val JAVA_PATH_SHORT_OPT = "j"
        private const val BUILD_MANUAL_TARGETS_OPT = "m"
        private const val DIRECTORIES_SHORT_OPT = "r"
        private const val EXCLUDED_DIRECTORIES_LONG_OPT = "excluded-directories"
        private const val DERIVE_TARGETS_FLAG_SHORT_OPT = "v"
        private const val IMPORT_DEPTH_SHORT_OPT = "i"
        private const val USE_BLOOP_SHORT_OPT = "u"
        private const val  BAZEL_WORKSPACE_ROOT_DIR_OPT ="w"

        const val INSTALLER_BINARY_NAME = "bazelbsp-install"
    }
}
