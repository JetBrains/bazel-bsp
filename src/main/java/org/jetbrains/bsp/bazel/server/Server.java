package org.jetbrains.bsp.bazel.server;

import ch.epfl.scala.bsp4j.BuildClient;
import io.grpc.ServerBuilder;
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
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.jetbrains.bsp.bazel.common.Constants;
import org.jetbrains.bsp.bazel.server.logger.BuildClientLogger;

public class Server {
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
      Path log = rootDir.resolve(Constants.BAZELBSP_LOG_FILE_NAME);
      PrintStream logStream =
          new PrintStream(
              Files.newOutputStream(
                  log, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

      Path traceFile = rootDir.resolve(Constants.BAZELBSP_TRACE_JSON_FILE_NAME);
      PrintWriter traceWriter =
          new PrintWriter(
              Files.newOutputStream(
                  traceFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
      System.setOut(logStream);
      System.setErr(logStream);

      BspIntegration bspIntegration = new BspIntegration(stdout, stdin, executor, traceWriter);
      BazelBspServerConfig serverConfig = BazelBspServerConfig.from(args);
      BazelBspServer bspServer = new BazelBspServer(serverConfig, bspIntegration);

      io.grpc.Server server = ServerBuilder.forPort(0).addService(bspServer.getBepServer()).build().start();
      bspServer.setBesBackendPort(server.getPort());

      bspIntegration.getLauncher().startListening();
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
}
