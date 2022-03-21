package org.jetbrains.bsp.bazel.install;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import com.google.common.base.Splitter;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextSingletonEntity;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextConstructor;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;

public class Install {

  public static final String INSTALLER_BINARY_NAME = "bazelbsp-install";
  public static final String SERVER_CLASS_NAME = "org.jetbrains.bsp.bazel.server.ServerInitializer";

  private static final String HELP_SHORT_OPT = "h";
  private static final String DIRECTORY_SHORT_OPT = "d";
  private static final String PROJECT_VIEW_FILE_PATH_SHORT_OPT = "p";

  public static void main(String[] args) throws IOException {
    // This is the command line which will be used to start the BSP server. See:
    // https://build-server-protocol.github.io/docs/server-discovery.html#build-tool-commands-to-start-bsp-servers
    var argv = new ArrayList<String>();

    var cliOptions = getCliOptions();
    var parser = new DefaultParser();
    var formatter = new HelpFormatter();
    formatter.setWidth(120);
    var hasError = false;
    var writer = new PrintWriter(System.err);
    try {
      var cmd = parser.parse(cliOptions, args, false);
      if (cmd.hasOption(HELP_SHORT_OPT)) {
        formatter.printHelp(INSTALLER_BINARY_NAME, cliOptions);
      } else {
        var rootDir = getRootDir(cmd);
        var bazelbspDir = createDir(rootDir, Constants.BAZELBSP_DIR_NAME);

        var defaultProjectViewFilePath = bazelbspDir.resolve("default-projectview.bazelproject");
        Files.copy(
            Install.class.getResourceAsStream("default-projectview.bazelproject"),
            defaultProjectViewFilePath,
            StandardCopyOption.REPLACE_EXISTING);

        copyAspects(bazelbspDir);
        createEmptyBuildFile(bazelbspDir);

        var pathToProjectViewFile = getProjectViewPath(cmd, defaultProjectViewFilePath);
        var projectViewProvider =
            new ProjectViewDefaultParserProvider(rootDir, pathToProjectViewFile);
        var projectView = projectViewProvider.create();

        var installationContextConstructor = new InstallationContextConstructor();
        var installationContext = installationContextConstructor.construct(projectView).get();

        addJavaBinary(argv, installationContext);
        addJavaClasspath(argv);
        addDebuggerConnection(argv, installationContext);
        argv.add(SERVER_CLASS_NAME);
        addProjectViewFilePath(argv, pathToProjectViewFile);

        BspConnectionDetails details = createBspConnectionDetails(argv);
        writeConfigurationFiles(rootDir, details);

        System.out.println(
            "Bazel BSP server installed in '" + rootDir.toAbsolutePath().normalize() + "'.");
      }
    } catch (ParseException e) {
      writer.println(e.getMessage());
      formatter.printUsage(writer, 120, INSTALLER_BINARY_NAME, cliOptions);
      hasError = true;
    } catch (NoSuchElementException | IllegalStateException e) {
      writer.println(e.getMessage());
      hasError = true;
    } finally {
      writer.close();
    }

    if (hasError) {
      System.exit(1);
    }
  }

  private static Path getRootDir(CommandLine cmd) {
    return cmd.hasOption(DIRECTORY_SHORT_OPT)
        ? Paths.get(cmd.getOptionValue(DIRECTORY_SHORT_OPT))
        : Paths.get("");
  }

  private static Path getProjectViewPath(CommandLine cmd, Path pathToDefaultProjectViewFile) {
    return cmd.hasOption(PROJECT_VIEW_FILE_PATH_SHORT_OPT)
        ? Paths.get(cmd.getOptionValue(PROJECT_VIEW_FILE_PATH_SHORT_OPT))
        : pathToDefaultProjectViewFile;
  }

  private static BspConnectionDetails createBspConnectionDetails(List<String> argv) {
    return new BspConnectionDetails(
        Constants.NAME,
        argv,
        Constants.VERSION,
        Constants.BSP_VERSION,
        Constants.SUPPORTED_LANGUAGES);
  }

  private static void addJavaBinary(List<String> argv, InstallationContext installationContext) {
    argv.add(installationContext.getJavaPath().getValue().toString());
  }

  private static void addJavaClasspath(List<String> argv) {
    argv.add("-classpath");
    String javaClassPath = readSystemProperty("java.class.path");
    String classpath =
        Splitter.on(":").splitToList(javaClassPath).stream()
            .map(elem -> Paths.get(elem).toAbsolutePath().toString())
            .collect(Collectors.joining(":"));

    argv.add(classpath);
  }

  private static void addDebuggerConnection(
      List<String> argv, InstallationContext installationContext) {
    installationContext
        .getDebuggerAddress()
        .map(ExecutionContextSingletonEntity::getValue)
        .forEach(
            debuggerAddress ->
                argv.add(
                    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="
                        + debuggerAddress.toString()));
  }

  private static void addProjectViewFilePath(List<String> argv, Path projectViewFilePath) {
    argv.add(projectViewFilePath.toString());
  }

  private static void copyAspects(Path bazelbspDir) throws IOException {
    var aspectsFile = bazelbspDir.resolve(Constants.ASPECTS_FILE_NAME);
    Files.copy(
        Install.class.getResourceAsStream(Constants.ASPECTS_FILE_NAME),
        aspectsFile,
        StandardCopyOption.REPLACE_EXISTING);
  }

  /** Create an empty BUILD file, overwrite if exists */
  private static void createEmptyBuildFile(Path bazelbspDir) throws IOException {
    Files.newByteChannel(
        bazelbspDir.resolve(Constants.BUILD_FILE_NAME),
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE);
  }

  private static Path createDir(Path rootDir, String name) throws IOException {
    var dir = rootDir.resolve(name);
    Files.createDirectories(dir);
    return dir;
  }

  /** Write bazelbsp.json file which allows bsp server discovery */
  private static void writeBazelbspJson(Path bspDir, Object discoveryDetails) throws IOException {
    Files.writeString(
        bspDir.resolve(Constants.BAZELBSP_JSON_FILE_NAME),
            new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(discoveryDetails));
  }

  private static void writeConfigurationFiles(Path rootDir, Object discoveryDetails)
      throws IOException {
    Path bspDir = createDir(rootDir, Constants.BSP_DIR_NAME);
    writeBazelbspJson(bspDir, discoveryDetails);
  }

  private static Options getCliOptions() {
    var cliOptions = new Options();

    var projectViewFilePath =
        Option.builder(PROJECT_VIEW_FILE_PATH_SHORT_OPT)
            .longOpt("project_view_file")
            .hasArg()
            .argName("path")
            .desc("Path to project view file.")
            .build();
    cliOptions.addOption(projectViewFilePath);

    var directory =
        Option.builder(DIRECTORY_SHORT_OPT)
            .longOpt("directory")
            .hasArg()
            .argName("path")
            .desc(
                "Path to directory where bazel bsp server should be setup. "
                    + "Current directory will be used by default")
            .build();
    cliOptions.addOption(directory);

    var help = Option.builder(HELP_SHORT_OPT).longOpt("help").desc("Show help").build();
    cliOptions.addOption(help);

    return cliOptions;
  }

  private static String readSystemProperty(String name) {
    String property = System.getProperty(name);
    if (property == null) {
      throw new NoSuchElementException("Could not read " + name + " system property");
    }
    return property;
  }
}
