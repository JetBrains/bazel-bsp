package org.jetbrains.bsp;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import ch.epfl.scala.bsp4j.BuildClient;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.lsp4j.jsonrpc.Launcher;

public class Main {
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.printf("Expected a command; got args: %s%n", Arrays.toString(args));
      System.exit(1);
    }
    if (args[0].equals("install")) {
      handleInstall(args.length > 1 ? Paths.get(args[1]) : null);
    } else if (args[0].equals("bsp")) {
      PrintStream stdout = System.out;
      InputStream stdin = System.in;

      Path home = getBazelBspPath();
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
        org.jetbrains.bsp.BazelBspServer bspServer = new org.jetbrains.bsp.BazelBspServer(args[1]);
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
        Server server =
            ServerBuilder.forPort(0)
                .addService(bepServer)
                .build()
                .start();
        bspServer.setBackendPort(server.getPort());
        launcher.startListening();
        server.awaitTermination();
      } finally {
        executor.shutdown();
      }
    } else if (args[0].equals("bep")) {
      String bazel = args.length > 1 ? args[1] : findOnPath("bazel");
      Server bepServer =
          ServerBuilder.forPort(0)
              .addService(new BepServer(new BazelBspServer(bazel), null))
              .build()
              .start();
      bepServer.awaitTermination();
    }
  }

  private static Path getBazelBspPath() {
    return Paths.get(".bazelbsp").toAbsolutePath();
  }

  private static void handleInstall(Path path) throws IOException {
    List<String> argv = new ArrayList<>();
    argv.add(Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString());
    argv.add("-classpath");
    Splitter.on(":").splitToList(System.getProperty("java.class.path")).stream()
        .map(elem -> Paths.get(elem).toAbsolutePath().toString())
        .forEach(argv::add);
    argv.add("org.jetbrains.bsp.Main");
    argv.add("bsp");
    argv.add(findOnPath("bazel"));
    BspConnectionDetails details =
        new BspConnectionDetails(
            org.jetbrains.bsp.Constants.NAME,
            argv,
            org.jetbrains.bsp.Constants.VERSION,
            Constants.BSP_VERSION,
            Lists.newArrayList("scala", "java"));
    Path bspDir = path == null ? Paths.get(".bsp") : path.resolve(".bsp");
    Files.createDirectories(bspDir);
    String aspectsFile = "aspects.bzl";
    Path home = getBazelBspPath();
    Files.createDirectories(home);
    Files.copy(
            Main.class.getResourceAsStream(aspectsFile),
            home.resolve(aspectsFile),
            StandardCopyOption.REPLACE_EXISTING
    );
    Files.newByteChannel(
            home.resolve("BUILD"),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
    );
    Files.write(
        bspDir.resolve("bazelbsp.json"),
        new GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(details)
            .getBytes(StandardCharsets.UTF_8));
  }

  private static String findOnPath(String bin) {
    List<String> pathElements = Splitter.on(File.pathSeparator).splitToList(System.getenv("PATH"));
    for (String pathElement : pathElements) {
      File maybePath = new File(pathElement, bin);
      if (maybePath.canExecute()) {
        return maybePath.toString();
      }
    }
    throw new NoSuchElementException("Could not find bazel on your PATH");
  }
}
