package org.jetbrains.bsp.bazel.server;

import io.grpc.Server;
import io.vavr.collection.Iterator;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.Executors;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData;
import org.jetbrains.bsp.bazel.server.bsp.config.BazelBspServerConfig;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;

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
      var bspInfo = new BspInfo();
      var rootDir = bspInfo.bazelBspDir();
      Files.createDirectories(rootDir);

      var traceFile = rootDir.resolve(Constants.BAZELBSP_TRACE_JSON_FILE_NAME);
      var traceWriter =
          new PrintWriter(
              Files.newOutputStream(
                  traceFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

      var bazelBspServerConfig = getBazelBspServerConfig(bspInfo, args);

      var bspIntegrationData = new BspIntegrationData(stdout, stdin, executor, traceWriter);
      var bspServer = new BazelBspServer(bazelBspServerConfig);
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

  private static BazelBspServerConfig getBazelBspServerConfig(BspInfo bspInfo, String[] args) {
    var projectViewPath = Iterator.of(args).headOption().map(Paths::get);
    return new BazelBspServerConfig(projectViewPath, bspInfo);
  }
}
