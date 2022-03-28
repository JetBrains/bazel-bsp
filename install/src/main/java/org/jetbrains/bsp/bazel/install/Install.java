package org.jetbrains.bsp.bazel.install;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.cli.ParseException;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.install.cli.CliOptionsProvider;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextConstructor;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;

public class Install {

  public static final String INSTALLER_BINARY_NAME = "bazelbsp-install";
  //  public static final String SERVER_CLASS_NAME =
  // "org.jetbrains.bsp.bazel.server.ServerInitializer";

  public static void main(String[] args) throws IOException {
    // This is the command line which will be used to start the BSP server. See:
    // https://build-server-protocol.github.io/docs/server-discovery.html#build-tool-commands-to-start-bsp-servers
    var argv = new ArrayList<String>();
    var hasError = false;
    var writer = new PrintWriter(System.err);
    try {
      var cliOptionsProvider = new CliOptionsProvider(args);

      if (cliOptionsProvider.isHelpOptionUsed()) {
        cliOptionsProvider.printHelp();
      } else {

        var cliOptions = cliOptionsProvider.getOptions();
        var rootDir = cliOptions.getWorkspaceRootDir();
        var bazelbspDir = createDir(rootDir, Constants.BAZELBSP_DIR_NAME);

        var defaultProjectViewFilePath = bazelbspDir.resolve("default-projectview.bazelproject");
        Files.copy(
            Install.class.getResourceAsStream("/default-projectview.bazelproject"),
            defaultProjectViewFilePath,
            StandardCopyOption.REPLACE_EXISTING);
        copyAspects(bazelbspDir);
        createEmptyBuildFile(bazelbspDir);

        var projectViewFilePath = cliOptions.getProjectViewFilePath();
        var projectViewProvider =
            new ProjectViewDefaultParserProvider(rootDir, projectViewFilePath);
        var projectView = projectViewProvider.create();

        var installationContextConstructor = new InstallationContextConstructor();
        var installationContext = installationContextConstructor.construct(projectView).get();

        var bspConnectionDetailsCreator =
            new BspConnectionDetailsCreator(installationContext, projectViewFilePath);
        var bspConnectionDetails = bspConnectionDetailsCreator.create().get();

        writeConfigurationFiles(rootDir, bspConnectionDetails);

        System.out.println(
            "Bazel BSP server installed in '" + rootDir.toAbsolutePath().normalize() + "'.");
      }
    } catch (ParseException | NoSuchElementException | IllegalStateException e) {
      writer.println(e.getMessage());
      hasError = true;
    } finally {
      writer.close();
    }

    if (hasError) {
      System.exit(1);
    }
  }

  private static BspConnectionDetails createBspConnectionDetails(List<String> argv) {
    return new BspConnectionDetails(
        Constants.NAME,
        argv,
        Constants.VERSION,
        Constants.BSP_VERSION,
        Constants.SUPPORTED_LANGUAGES);
  }

  private static void copyAspects(Path bazelbspDir) throws IOException {
    var aspectsFile = bazelbspDir.resolve(Constants.ASPECTS_FILE_NAME);
    Files.copy(
        Install.class.getResourceAsStream("/" + Constants.ASPECTS_FILE_NAME),
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
        new GsonBuilder().setPrettyPrinting().create().toJson(discoveryDetails));
  }

  private static void writeConfigurationFiles(Path rootDir, Object discoveryDetails)
      throws IOException {
    Path bspDir = createDir(rootDir, Constants.BSP_DIR_NAME);
    writeBazelbspJson(bspDir, discoveryDetails);
  }
}
