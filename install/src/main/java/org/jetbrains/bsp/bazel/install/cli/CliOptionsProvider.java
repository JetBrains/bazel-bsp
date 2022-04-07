package org.jetbrains.bsp.bazel.install.cli;

import io.vavr.control.Try;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CliOptionsProvider {

  private static final String HELP_SHORT_OPT = "h";
  private static final Option helpOption =
      Option.builder(HELP_SHORT_OPT).longOpt("help").desc("Show help").build();

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

  private final String[] args;
  private final Options cliParserOptions;

  public CliOptionsProvider(String[] args) {
    this.args = args;

    this.cliParserOptions = getCliParserOptions();
  }

  public Try<CliOptions> getOptions() {
    var parser = new DefaultParser();

    return Try.of(() -> parser.parse(cliParserOptions, args, false)).map(this::createCliOptions);
  }

  private static Options getCliParserOptions() {
    var cliOptions = new Options();
    cliOptions.addOption(projectViewFilePathOption);
    cliOptions.addOption(directoryOption);
    cliOptions.addOption(helpOption);

    return cliOptions;
  }

  private CliOptions createCliOptions(CommandLine cmd) {
    return new CliOptions(
        isHelpOptionUsed(cmd), this::printHelp, getWorkspaceRootDir(cmd), getProjectViewPath(cmd));
  }

  private boolean isHelpOptionUsed(CommandLine cmd) {
    return cmd.hasOption(HELP_SHORT_OPT);
  }

  private void printHelp() {
    var formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp(INSTALLER_BINARY_NAME, cliParserOptions);
  }

  private Path getWorkspaceRootDir(CommandLine cmd) {
    return cmd.hasOption(DIRECTORY_SHORT_OPT)
        ? Paths.get(cmd.getOptionValue(DIRECTORY_SHORT_OPT))
        : getCurrentDirectory();
  }

  private io.vavr.control.Option<Path> getProjectViewPath(CommandLine cmd) {
    return io.vavr.control.Option.when(
        cmd.hasOption(PROJECT_VIEW_FILE_PATH_SHORT_OPT),
        () -> calculateAbsoluteProjectViewPath(cmd));
  }

  private Path calculateAbsoluteProjectViewPath(CommandLine cmd) {
    var currentDir = getCurrentDirectory();
    var projectViewPath = Paths.get(cmd.getOptionValue(PROJECT_VIEW_FILE_PATH_SHORT_OPT));

    return currentDir.resolve(projectViewPath).normalize();
  }

  private Path getCurrentDirectory() {
    return Paths.get("").toAbsolutePath();
  }
}
