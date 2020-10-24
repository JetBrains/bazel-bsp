package org.jetbrains.bsp.bazel;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import com.google.common.base.Splitter;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Install {
  public static void main(String[] args) throws IOException {
    List<String> argv = new ArrayList<>();
    argv.add(Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java").toString());
    argv.add("-classpath");
    Splitter.on(":").splitToList(System.getProperty("java.class.path")).stream()
        .map(elem -> Paths.get(elem).toAbsolutePath().toString())
        .forEach(argv::add);
    argv.add("org.jetbrains.bsp.bazel.Server");
    argv.add("bsp");
    argv.add(findOnPath("bazel"));
    BspConnectionDetails details =
        new BspConnectionDetails(
            Constants.NAME,
            argv,
            Constants.VERSION,
            Constants.BSP_VERSION,
            BazelBspServer.SUPPORTED_LANGUAGES);
    Path bspDir = args.length > 1 ? Paths.get(args[1]).resolve(".bsp") : Paths.get(".bsp");
    Files.createDirectories(bspDir);
    String aspectsFile = "aspects.bzl";
    Path home = Paths.get(".bazelbsp").toAbsolutePath();
    Files.createDirectories(home);
    Files.copy(
        Server.class.getResourceAsStream(aspectsFile),
        home.resolve(aspectsFile),
        StandardCopyOption.REPLACE_EXISTING);
    Files.newByteChannel(
        home.resolve("BUILD"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    Files.write(
        bspDir.resolve("bazelbsp.json"),
        new GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(details)
            .getBytes(StandardCharsets.UTF_8));
  }

  protected static String findOnPath(String bin) {
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
