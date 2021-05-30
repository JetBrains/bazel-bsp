package org.jetbrains.bsp.bazel.server;

import io.grpc.Server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParser;
import org.jetbrains.bsp.bazel.projectview.parser.ProjectViewParserFactory;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.config.BazelBspServerConfig;

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
      Path rootDir = Paths.get(Constants.BAZELBSP_DIR_NAME).toAbsolutePath();
      Files.createDirectories(rootDir);

      Path traceFile = rootDir.resolve(Constants.BAZELBSP_TRACE_JSON_FILE_NAME);
      PrintWriter traceWriter =
          new PrintWriter(
              Files.newOutputStream(
                  traceFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

      BspIntegrationData bspIntegrationData =
          new BspIntegrationData(stdout, stdin, executor, traceWriter);
      BazelBspServerConfig serverConfig = BazelBspServerConfig.from(args);
      BazelBspServer bspServer = new BazelBspServer(serverConfig);
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

  private static Optional<ProjectView> getProjectViewIfExists() throws IOException {
    ProjectViewParser parser = ProjectViewParserFactory.getBasic();
    File projectViewFile = new File(Constants.DEFAULT_PROJECT_VIEW_FILE);

    if (projectViewFile.isFile()) {
      return Optional.of(parser.parse(projectViewFile));
    }

    return Optional.empty();
  }
}
