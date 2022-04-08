package org.jetbrains.bsp.bazel.install;

import ch.epfl.scala.bsp4j.BspConnectionDetails;
import com.google.gson.GsonBuilder;
import io.vavr.control.Try;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.jetbrains.bsp.bazel.commons.Constants;

public class EnvironmentCreator {

  private final Path projectRootDir;
  private final BspConnectionDetails discoveryDetails;

  public EnvironmentCreator(Path projectRootDir, BspConnectionDetails discoveryDetails) {
    this.projectRootDir = projectRootDir;
    this.discoveryDetails = discoveryDetails;
  }

  public Try<Void> create() {
    return createDotBazelBsp().flatMap(__ -> createDotBsp());
  }

  private Try<Void> createDotBazelBsp() {
    return createDir(projectRootDir, Constants.DOT_BAZELBSP_DIR_NAME)
        .flatMap(this::createDotBazelBspFiles);
  }

  private Try<Void> createDotBazelBspFiles(Path dotBazelBspDir) {
    return copyAspects(dotBazelBspDir)
        .flatMap(__ -> createEmptyBuildFile(dotBazelBspDir))
        .flatMap(__ -> copyDefaultProjectViewFilePath(dotBazelBspDir));
  }

  private Try<Void> copyAspects(Path dotBazelBspDir) {
    var resourcesAspectsPath = "/" + Constants.ASPECTS_FILE_NAME;
    var destinationAspectsPath = dotBazelBspDir.resolve(Constants.ASPECTS_FILE_NAME);

    return copyFileFromResources(resourcesAspectsPath, destinationAspectsPath);
  }

  private Try<Void> createEmptyBuildFile(Path dotBazelBspDir) {
    var destinationBuildFilePath = dotBazelBspDir.resolve(Constants.BUILD_FILE_NAME);

    return Try.run(() -> destinationBuildFilePath.toFile().createNewFile());
  }

  private Try<Void> copyDefaultProjectViewFilePath(Path dotBazelBspDir) {
    var resourcesProjectViewFilePath = "/default-projectview.bazelproject";
    var destinationProjectViewFilePath = dotBazelBspDir.resolve("default-projectview.bazelproject");

    return copyFileFromResources(resourcesProjectViewFilePath, destinationProjectViewFilePath);
  }

  private Try<Void> copyFileFromResources(String resourcesPath, Path destinationPath) {
    return Try.withResources(() -> EnvironmentCreator.class.getResourceAsStream(resourcesPath))
        .of(inputStream -> copyFile(inputStream, destinationPath))
        .flatMap(x -> x);
  }

  private Try<Void> copyFile(InputStream inputStream, Path destinationPath) {
    return Try.run(
        () -> Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING));
  }

  private Try<Void> createDotBsp() {
    return createDir(projectRootDir, Constants.DOT_BSP_DIR_NAME)
        .flatMap(this::createBspDiscoveryDetailsFile);
  }

  private Try<Path> createDir(Path rootDir, String name) {
    var dir = rootDir.resolve(name);
    return Try.of(() -> Files.createDirectories(dir));
  }

  private Try<Void> createBspDiscoveryDetailsFile(Path dotBspDir) {
    var destinationBspDiscoveryFilePath = dotBspDir.resolve(Constants.BAZELBSP_JSON_FILE_NAME);
    var fileContent = new GsonBuilder().setPrettyPrinting().create().toJson(discoveryDetails);

    return writeStringToFile(destinationBspDiscoveryFilePath, fileContent);
  }

  private Try<Void> writeStringToFile(Path destinationPath, String string) {
    return Try.run(() -> Files.writeString(destinationPath, string));
  }
}
