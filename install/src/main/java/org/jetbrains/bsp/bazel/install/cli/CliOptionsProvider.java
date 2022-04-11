package org.jetbrains.bsp.bazel.install.cli;

import com.google.common.net.HostAndPort;
import io.vavr.collection.List;
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

  private static final String TARGETS_SHORT_OPT = "t";
  private static final Option targetsOption =
          Option.builder(TARGETS_SHORT_OPT)
                  .longOpt("targets")
                  .hasArg()
                  .argName("target")
                  .desc("to do")
                  .build();

  private static final String BUILD_FLAGS_SHORT_OPT = "f";

  private static final Option buildFlagsOption =
          Option.builder(BUILD_FLAGS_SHORT_OPT)
                  .longOpt("build_flags")
                  .hasArg()
                  .argName("flag")
                  .desc("to do")
                  .build();

  private static final String INCLUDE_SHORT_OPT = "i";

  private static final Option includeOption =
          Option.builder(INCLUDE_SHORT_OPT)
                  .longOpt("include")
                  .hasArg()
                  .argName("statement")
                  .desc("to do")
                  .build();

  private static final String BAZEL_PATH_SHORT_OPT = "b";

  private static final Option bazelPathOption =
          Option.builder(BAZEL_PATH_SHORT_OPT)
                  .longOpt("bazel_path")
                  .hasArg()
                  .argName("path")
                  .desc("to do")
                  .build();

  private static final String DEBUGGER_ADDRESS_SHORT_OPT = "x";

  private static final Option debuggerAddressOption =
          Option.builder(DEBUGGER_ADDRESS_SHORT_OPT)
                  .longOpt("debugger_address")
                  .hasArg()
                  .argName("address")
                  .desc("to do")
                  .build();

  private static final String JAVA_PATH_SHORT_OPT = "j";

  private static final Option javaPathOption =
          Option.builder(JAVA_PATH_SHORT_OPT)
                  .longOpt("java_path")
                  .hasArg()
                  .argName("path")
                  .desc("to do")
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
    cliOptions.addOption(targetsOption);
    cliOptions.addOption(buildFlagsOption);
    cliOptions.addOption(includeOption);
    cliOptions.addOption(bazelPathOption);
    cliOptions.addOption(debuggerAddressOption);
    cliOptions.addOption(javaPathOption);

    return cliOptions;
  }

  private CliOptions createCliOptions(CommandLine cmd) {
    return new CliOptions(
            isHelpOptionUsed(cmd),
            this::printHelp,
            getWorkspaceRootDir(cmd),
            getProjectViewFilePath(cmd),
            getJavaPath(cmd),
            getBazelPath(cmd),
            getDebuggerAddress(cmd),
            getTarget(cmd),
            getBuildFlags(cmd),
            getInclude(cmd));
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
            : Paths.get("");
  }

  private io.vavr.control.Option<Path> getProjectViewFilePath(CommandLine cmd) {
    return io.vavr.control.Option.when(
            cmd.hasOption(PROJECT_VIEW_FILE_PATH_SHORT_OPT),
            () -> Paths.get(cmd.getOptionValue(PROJECT_VIEW_FILE_PATH_SHORT_OPT)));
  }

  private io.vavr.control.Option<String> getJavaPath(CommandLine cmd) {
    return getOptionValue(cmd, JAVA_PATH_SHORT_OPT);
  }

  private io.vavr.control.Option<String> getBazelPath(CommandLine cmd) {
    return getOptionValue(cmd, BAZEL_PATH_SHORT_OPT);
  }

  private io.vavr.control.Option<HostAndPort> getDebuggerAddress(CommandLine cmd) {
    return getOptionValue(cmd, DEBUGGER_ADDRESS_SHORT_OPT).map(HostAndPort::fromString);
  }

  private io.vavr.control.Option<List<String>> getTarget(CommandLine cmd) {
    return getOptionLListValue(cmd, TARGETS_SHORT_OPT);
  }

  private io.vavr.control.Option<List<String>> getBuildFlags(CommandLine cmd) {
    return getOptionLListValue(cmd, BUILD_FLAGS_SHORT_OPT);
  }

  private io.vavr.control.Option<List<String>> getInclude(CommandLine cmd) {
    return getOptionLListValue(cmd, INCLUDE_SHORT_OPT);
  }

  private io.vavr.control.Option<String> getOptionValue(CommandLine cmd, String shortOpt) {
    return io.vavr.control.Option.when(cmd.hasOption(shortOpt), () -> cmd.getOptionValue(shortOpt));
  }

  private io.vavr.control.Option<List<String>> getOptionLListValue(
          CommandLine cmd, String shortOpt) {
    return io.vavr.control.Option.when(cmd.hasOption(shortOpt), () -> cmd.getOptionValues(shortOpt))
            .map(List::of);
  }
}
