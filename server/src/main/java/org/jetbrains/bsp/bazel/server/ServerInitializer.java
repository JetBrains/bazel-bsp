package org.jetbrains.bsp.bazel.server;

import io.grpc.Server;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.ProjectViewProvider;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewDefaultParserProvider;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.config.BazelBspServerConfig;
import org.jetbrains.bsp.bazel.server.bsp.config.ServerArgsProjectViewProvider;

public class ServerInitializer {

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.printf("Expected path to bazel; got args: %s%n", Arrays.toString(args));
      System.exit(1);
    }

    boolean hasErrors = false;
    PrintStream stdout = System.out;
    InputStream stdin = System.in;
    ExecutorService executor = Executors.newCachedThreadPool();

    try {
      var bspProjectRoot = Paths.get("").toAbsolutePath();
      var rootDir = bspProjectRoot.resolve(Constants.BAZELBSP_DIR_NAME);
      Files.createDirectories(rootDir);

      var traceFile = rootDir.resolve(Constants.BAZELBSP_TRACE_JSON_FILE_NAME);
      PrintWriter traceWriter =
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
    var pathToBazel = projectView.getBazelPath().get().getValue();
    return new BazelBspServerConfig(pathToBazel, projectView);
  }

  private static ProjectView getProjectView(Path bspProjectRoot, String[] args) {
    ProjectViewProvider provider = getProjectViewProvider(bspProjectRoot, args);

    return provider.create().get();
  }

  private static ProjectViewProvider getProjectViewProvider(Path bspProjectRoot, String[] args) {
    if (args.length == 1) {
      return new ServerArgsProjectViewProvider(bspProjectRoot, args[0]);
    }
    if (args.length == 2) {
      return new ServerArgsProjectViewProvider(bspProjectRoot, args[0], args[1]);
    }

    return new ProjectViewDefaultParserProvider(bspProjectRoot);
  }
}
