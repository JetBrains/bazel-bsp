package org.jetbrains.bsp.bazel;

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

public class Server {
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.printf("Expected a command; got args: %s%n", Arrays.toString(args));
      System.exit(1);
    }

    if (args[0].equals("bsp")) {
      PrintStream stdout = System.out;
      InputStream stdin = System.in;

      Path home = Paths.get(".bazelbsp").toAbsolutePath();
      Files.createDirectories(home);
      Path log = home.resolve("bazelbsp.log");
      PrintStream logStream =
          new PrintStream(
              Files.newOutputStream(
                  log, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

      Path traceFile = home.resolve("bazelbsp.trace.json");
      PrintWriter traceWriter =
          new PrintWriter(
              Files.newOutputStream(
                  traceFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
      System.setOut(logStream);
      System.setErr(logStream);
      ExecutorService executor = Executors.newCachedThreadPool();
      try {
        BazelBspServer bspServer = new BazelBspServer(args[1]);
        Launcher<BuildClient> launcher =
            new Launcher.Builder()
                .traceMessages(traceWriter)
                .setOutput(stdout)
                .setInput(stdin)
                .setLocalService(bspServer)
                .setRemoteInterface(BuildClient.class)
                .setExecutorService(executor)
                .create();
        bspServer.setBuildClient(launcher.getRemoteProxy());
        BepServer bepServer = new BepServer(bspServer, launcher.getRemoteProxy());
        bspServer.bepServer = bepServer;
        io.grpc.Server server = ServerBuilder.forPort(0).addService(bepServer).build().start();
        bspServer.setBackendPort(server.getPort());
        launcher.startListening();
        server.awaitTermination();
      } finally {
        executor.shutdown();
      }
    } else if (args[0].equals("bep")) {
      String bazel = args.length > 1 ? args[1] : Install.findOnPath("bazel");
      io.grpc.Server bepServer =
          ServerBuilder.forPort(0)
              .addService(new BepServer(new BazelBspServer(bazel), null))
              .build()
              .start();
      bepServer.awaitTermination();
    }
  }
}
