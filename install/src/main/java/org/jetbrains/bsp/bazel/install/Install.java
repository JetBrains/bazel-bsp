package org.jetbrains.bsp.bazel.install;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.install.cli.CliOptions;
import org.jetbrains.bsp.bazel.install.cli.CliOptionsProvider;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextConstructor;

public class Install {

  public static void main(String[] args) {
    var cliOptionsProvider = new CliOptionsProvider(args);
    var cliOptions = cliOptionsProvider.getOptions().get();

    if (cliOptions.isHelpOptionUsed()) {
      cliOptions.printHelp();
    } else {
      createEnvironmentAndInstallBazelBspServer(cliOptions)
          .onSuccess(__ -> printInCaseOfSuccess(cliOptions))
          .onFailure(Install::printFailureReasonAndExit1);
    }
  }

  private static Try<Void> createEnvironmentAndInstallBazelBspServer(CliOptions cliOptions) {
    return constructInstallationContext(cliOptions)
        .flatMap(installationContext -> createBspConnectionDetails(installationContext, cliOptions))
        .flatMap(details -> createEnvironment(cliOptions, details));
  }

  private static Try<InstallationContext> constructInstallationContext(CliOptions cliOptions) {
    var resourcesProjectViewFilePath = "/default-projectview.bazelproject";
    var projectViewProvider =
        new ProjectViewDefaultFromResourcesProvider(
            cliOptions.getProjectViewFilePath(), resourcesProjectViewFilePath);
    var projectViewTry = projectViewProvider.create();

    var installationContextConstructor = new InstallationContextConstructor();
    return installationContextConstructor.construct(projectViewTry);
  }

  private static Try<BspConnectionDetails> createBspConnectionDetails(
      InstallationContext installationContext, CliOptions cliOptions) {
    var bspConnectionDetailsCreator =
        new BspConnectionDetailsCreator(installationContext, cliOptions.getProjectViewFilePath());
    return bspConnectionDetailsCreator.create();
  }

  private static Try<Void> createEnvironment(CliOptions cliOptions, BspConnectionDetails details) {
    var environmentCreator = new EnvironmentCreator(cliOptions.getWorkspaceRootDir(), details);
    return environmentCreator.create();
  }

  private static void printInCaseOfSuccess(CliOptions cliOptions) {
    var absoluteDirWhereServerWasInstalledIn =
        cliOptions.getWorkspaceRootDir().toAbsolutePath().normalize();
    System.out.printf(
        "Bazel BSP server installed in '%s'.\n", absoluteDirWhereServerWasInstalledIn);
  }

  private static void printFailureReasonAndExit1(Throwable exception) {
    System.err.print("Bazel BSP server installation failed! Reason: ");
    exception.printStackTrace();
    System.exit(1);
  }
}
