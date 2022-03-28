package org.jetbrains.bsp.bazel.install.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliOptionsProvider {

  private static final String HELP_SHORT_OPT = "h";
  private static final String DIRECTORY_SHORT_OPT = "d";
  private static final Option directoryOption =
      Option.builder(DIRECTORY_SHORT_OPT)
          .longOpt("directory")
          .hasArg()
          .argName("path")
          .desc(
              "Path to directory where bazel bsp server should be setup. "
                  + "Current directory will be used by default")
          .build();

  private static final String PROJECT_VIEW_FILE_PATH_SHORT_OPT = "p";
  private static final Option projectViewFilePathOption =
      Option.builder(PROJECT_VIEW_FILE_PATH_SHORT_OPT)
          .longOpt("project_view_file")
          .hasArg()
          .argName("path")
          .desc("Path to project view file.")
          .build();

  public static final String INSTALLER_BINARY_NAME = "bazelbsp-install";

  private final CommandLine cmd;
  private final Options cliParserOptions;

  public CliOptionsProvider(String[] args) throws ParseException {
    this.cliParserOptions = getCliParserOptions();

    var parser = new DefaultParser();
    this.cmd = parser.parse(cliParserOptions, args, false);
  }

  public Try<CliOptions> getOptions() {
    var parser = new DefaultParser();

    return Try.of(() -> parser.parse(cliParserOptions, args, false))
            .map(this::createCliOptions);
  }

  private static Options getCliParserOptions() {
    var cliOptions = new Options();
    cliOptions.addOption(projectViewFilePathOption);
    cliOptions.addOption(directoryOption);
    cliOptions.addOption(helpOption);

    return cliOptions;
  }

  private static CliOptions createCliOptions(CommandLine cmd) {
    var workspaceRootDir = getWorkspaceRootDir(cmd);
    var defaultProjectViewFilePath = workspaceRootDir.resolve("default-projectview.bazelproject");

    return new CliOptions(
        isHelpOptionUsed(cmd),
        this::printHelp,
        workspaceRootDir,
        getProjectViewPath(cmd, defaultProjectViewFilePath));
  }

  private static Path getWorkspaceRootDir(CommandLine cmd) {
    return cmd.hasOption(DIRECTORY_SHORT_OPT)
        ? Paths.get(cmd.getOptionValue(DIRECTORY_SHORT_OPT))
        : Paths.get("");
  }

  private static Path getProjectViewPath(CommandLine cmd, Path pathToDefaultProjectViewFile) {
    return cmd.hasOption(PROJECT_VIEW_FILE_PATH_SHORT_OPT)
        ? Paths.get(cmd.getOptionValue(PROJECT_VIEW_FILE_PATH_SHORT_OPT))
        : pathToDefaultProjectViewFile;
  }
}
