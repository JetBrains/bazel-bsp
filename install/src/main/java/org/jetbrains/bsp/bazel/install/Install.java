package org.jetbrains.bsp.bazel.install;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import org.jetbrains.bsp.bazel.install.cli.CliOptionsProvider;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextConstructor;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;

public class Install {

  public static void main(String[] args) throws IOException {
    var hasError = false;
    var writer = new PrintWriter(System.err);
    try {
      var cliOptionsProvider = new CliOptionsProvider(args);
      var cliOptions = cliOptionsProvider.getOptions().get();

      if (cliOptions.isHelpOptionUsed()) {
        cliOptions.printHelp();
      } else {
        var rootDir = cliOptions.getWorkspaceRootDir();

        var projectViewFilePath = cliOptions.getProjectViewFilePath();
        var projectViewProvider =
            new ProjectViewDefaultParserProvider(rootDir, projectViewFilePath);
        var projectView = projectViewProvider.create();

        var installationContextConstructor = new InstallationContextConstructor();
        var installationContext = installationContextConstructor.construct(projectView).get();

        var bspConnectionDetailsCreator =
            new BspConnectionDetailsCreator(installationContext, projectViewFilePath);
        var bspConnectionDetails = bspConnectionDetailsCreator.create().get();

        var environmentCreator = new EnvironmentCreator(rootDir, bspConnectionDetails);
        environmentCreator.create().get();

        System.out.println(
            "Bazel BSP server installed in '" + rootDir.toAbsolutePath().normalize() + "'.");
      }
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
}
