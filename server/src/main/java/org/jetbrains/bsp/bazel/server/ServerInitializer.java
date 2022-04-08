package org.jetbrains.bsp.bazel.server;

import com.google.common.base.Splitter;
import io.grpc.Server;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.config.BazelBspServerConfig;

public class ServerInitializer {

  public static void main(String[] args) {
    if (args.length > 1) {
      System.err.printf(
          "Expected optional path to project view file; got too many args: %s%n",
          Arrays.toString(args));
      System.exit(1);
    }

    var hasErrors = false;
    var stdout = System.out;
    var stdin = System.in;
    var executor = Executors.newCachedThreadPool();

    try {
      var bspProjectRoot = Paths.get("").toAbsolutePath();
      var rootDir = bspProjectRoot.resolve(Constants.DOT_BAZELBSP_DIR_NAME);
      Files.createDirectories(rootDir);

      var traceFile = rootDir.resolve(Constants.BAZELBSP_TRACE_JSON_FILE_NAME);
      var traceWriter =
          new PrintWriter(
              Files.newOutputStream(
                  traceFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

      var bazelBspServerConfig = getBazelBspServerConfig(bspProjectRoot, args);

      BspIntegrationData bspIntegrationData =
          new BspIntegrationData(stdout, stdin, executor, traceWriter);
      BazelBspServer bspServer = new BazelBspServer(bazelBspServerConfig);
      bspServer.startServer(bspIntegrationData);

      Server server = bspIntegrationData.getServer().start();
      bspServer.setBesBackendPort(server.getPort());

      bspIntegrationData.getLauncher().startListening();
      server.awaitTermination();
    } catch (Exception e) {
      e.printStackTrace();
      hasErrors = true;
    } finally {
      executor.shutdown();
    }

    if (hasErrors) {
      System.exit(1);
    }
  }

  private static BazelBspServerConfig getBazelBspServerConfig(Path bspProjectRoot, String[] args) {
    var projectView = getProjectView(bspProjectRoot, args);
    var pathToBazel = getBazelPath(projectView);
    return new BazelBspServerConfig(pathToBazel, projectView);
  }

  private static ProjectView getProjectView(Path bspProjectRoot, String[] args) {
    var provider = getProjectViewProvider(bspProjectRoot, args);

    return provider.create().get();
  }

  private static ProjectViewProvider getProjectViewProvider(Path bspProjectRoot, String[] args) {
    if (args.length == 0) {
      return new ProjectViewDefaultParserProvider(bspProjectRoot);
    }

    var pathToProjectView = Paths.get(args[0]);
    return new ProjectViewDefaultParserProvider(bspProjectRoot, pathToProjectView);
  }

  private static String getBazelPath(ProjectView projectView) {
    return projectView
        .getBazelPath()
        .map(ProjectViewSingletonSection::getValue)
        .map(Path::toString)
        .getOrElse(findOnPath("bazel"));
  }

  private static String findOnPath(String bin) {
    var pathElements = Splitter.on(File.pathSeparator).splitToList(System.getenv("PATH"));

    return pathElements.stream()
        .filter(ServerInitializer::isItNotBazeliskPath)
        .map(element -> new File(element, bin))
        .filter(File::canExecute)
        .findFirst()
        .map(File::toString)
        .orElseThrow(() -> new NoSuchElementException("Could not find " + bin + " on your PATH"));
  }

  private static boolean isItNotBazeliskPath(String path) {
    return !path.contains("bazelisk/");
  }
}
