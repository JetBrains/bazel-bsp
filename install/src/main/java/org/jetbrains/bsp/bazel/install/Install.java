package org.jetbrains.bsp.bazel.install;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import io.vavr.control.Try;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.install.cli.CliOptions;
import org.jetbrains.bsp.bazel.install.cli.CliOptionsProvider;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContext;
import org.jetbrains.bsp.bazel.installationcontext.InstallationContextConstructor;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;

public class Install {

  //  public static void main(String[] args) {
  //    var hasError = false;
  //    try {
  //      var cliOptionsProvider = new CliOptionsProvider(args);
  //      var cliOptions = cliOptionsProvider.getOptions().get();
  //
  //      if (cliOptions.isHelpOptionUsed()) {
  //        cliOptions.printHelp();
  //      } else {
  //        var rootDir = cliOptions.getWorkspaceRootDir();
  //
  //        var projectViewFilePath = cliOptions.getProjectViewFilePath();
  //        var projectViewProvider =
  //            new ProjectViewDefaultParserProvider(rootDir, projectViewFilePath);
  //        var projectView = projectViewProvider.create();
  //
  //        var installationContextConstructor = new InstallationContextConstructor();
  //        var installationContext = installationContextConstructor.construct(projectView).get();
  //
  //        var bspConnectionDetailsCreator =
  //            new BspConnectionDetailsCreator(installationContext, projectViewFilePath);
  //        var bspConnectionDetails = bspConnectionDetailsCreator.create().get();
  //
  //        var environmentCreator = new EnvironmentCreator(rootDir, bspConnectionDetails);
  //        environmentCreator.create().get();
  //
  //        System.out.println(
  //            "Bazel BSP server installed in '" + rootDir.toAbsolutePath().normalize() + "'.");
  //      }
  //    } catch (NoSuchElementException | IllegalStateException e) {
  //      hasError = true;
  //    }
  //
  //    if (hasError) {
  //      System.exit(1);
  //    }
  //    }
  public static void main(String[] args) {
    var cliOptionsProvider = new CliOptionsProvider(args);
    var cliOptions = cliOptionsProvider.getOptions().get();

    if (cliOptions.isHelpOptionUsed()) {
      cliOptions.printHelp();
    } else {
      createEnvironmentAndInstallBazelBspServer(cliOptions)
          .onSuccess(__ -> printInCaseOfSuccess(cliOptions))
          .onFailure(__ -> printInCaseOfFailure());
    }
  }

  private static Try<Void> createEnvironmentAndInstallBazelBspServer(CliOptions cliOptions) {
    //    var rootDir = cliOptions.getWorkspaceRootDir();
    //    var projectViewFilePath = cliOptions.getProjectViewFilePath();
    //    var projectViewProvider = new ProjectViewDefaultParserProvider(rootDir,
    // projectViewFilePath);
    //
    //    var projectViewTry = projectViewProvider.create();
    //    var installationContextConstructor = new InstallationContextConstructor();
    //    var installationContextTry = installationContextConstructor.construct(projectViewTry);
    //
    //    return tryInstallationContextCreator(rootDir, projectViewFilePath)
    //        .map(
    //            installationContext ->
    //                new BspConnectionDetailsCreator(installationContext, projectViewFilePath))
    //        .flatMap(BspConnectionDetailsCreator::create)
    //        .map(details -> new EnvironmentCreator(rootDir, details))
    //        .flatMap(EnvironmentCreator::create);
    return tryInstallationContextCreator(cliOptions)
            .flatMap(installationContext -> tryBspConnectionDetailsCreator(installationContext, cliOptions.getProjectViewFilePath()))
            .flatMap(details -> environmentCreatorProvider(cliOptions, details));
//    return environmentCreatorProvider(cliOptions);
  }

  private static Try<InstallationContext> tryInstallationContextCreator(CliOptions cliOptions) {
    var rootDir = cliOptions.getWorkspaceRootDir();
    var projectViewFilePath = cliOptions.getProjectViewFilePath();

    var projectViewProvider = new ProjectViewDefaultParserProvider(rootDir, projectViewFilePath);
    var projectViewTry = projectViewProvider.create();

    var installationContextConstructor = new InstallationContextConstructor();
    return installationContextConstructor.construct(projectViewTry);
  }

  private static Try<BspConnectionDetails> tryBspConnectionDetailsCreator(InstallationContext installationContext, Path projectViewFilePath) {
    var bspConnectionDetailsCreator = new BspConnectionDetailsCreator(installationContext, projectViewFilePath);
    return bspConnectionDetailsCreator.create();
  }

  private static Try<Void> environmentCreatorProvider(CliOptions cliOptions, BspConnectionDetails details ) {
//    return tryBspConnectionDetailsCreator(cliOptions)
//        .map(details -> new EnvironmentCreator(cliOptions.getWorkspaceRootDir(), details))
//        .flatMap(EnvironmentCreator::create);
    var environmentCreator = new EnvironmentCreator(cliOptions.getWorkspaceRootDir(), details);
    return environmentCreator.create();
  }

  private static void printInCaseOfSuccess(CliOptions cliOptions) {
    System.out.println(
        "Bazel BSP server installed in '"
            + cliOptions.getWorkspaceRootDir().toAbsolutePath().normalize()
            + "'.");
  }
  private static void printInCaseOfFailure() {
    System.out.println("Bazel BSP server installation failed.");
    System.exit(1);
  }
}
