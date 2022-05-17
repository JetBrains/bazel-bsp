package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation;
import org.jetbrains.bsp.bazel.server.sync.model.Label;

public class BazelPathsResolver {
  private final BazelInfo bazelInfo;

  public BazelPathsResolver(BazelInfo bazelInfo) {
    this.bazelInfo = bazelInfo;
  }

  public URI workspaceRoot() {
    return bazelInfo.getWorkspaceRoot().toAbsolutePath().toUri();
  }

  public Seq<URI> resolveUris(java.util.List<FileLocation> fileLocations) {
    return fileLocations.stream().map(this::resolve).map(Path::toUri).collect(Array.collector());
  }

  public Seq<Path> resolvePaths(java.util.List<FileLocation> fileLocations) {
    return fileLocations.stream().map(this::resolve).collect(Array.collector());
  }

  public URI resolveUri(FileLocation fileLocation) {
    return resolve(fileLocation).toUri();
  }

  public Path resolve(FileLocation fileLocation) {
    if (isAbsolute(fileLocation)) {
      return resolveAbsolute(fileLocation);
    } else if (isMainWorkspaceSource(fileLocation)) {
      return resolveSource(fileLocation);
    } else {
      return resolveOutput(fileLocation);
    }
  }

  private boolean isAbsolute(FileLocation fileLocation) {
    var relative = fileLocation.getRelativePath();
    return (relative.startsWith("/") && Files.exists(Paths.get(relative)));
  }

  private Path resolveAbsolute(FileLocation fileLocation) {
    return Paths.get(fileLocation.getRelativePath());
  }

  private Path resolveOutput(FileLocation fileLocation) {
    return Paths.get(
        bazelInfo.getExecRoot(),
        fileLocation.getRootExecutionPathFragment(),
        fileLocation.getRelativePath());
  }

  private Path resolveSource(FileLocation fileLocation) {
    return bazelInfo.getWorkspaceRoot().resolve(fileLocation.getRelativePath());
  }

  private boolean isMainWorkspaceSource(FileLocation fileLocation) {
    return fileLocation.getIsSource() && !fileLocation.getIsExternal();
  }

  public Path labelToDirectory(Label label) {
    var relativePath = extractRelativePath(label.getValue());
    return bazelInfo.getWorkspaceRoot().resolve(relativePath);
  }

  private String extractRelativePath(String label) {
    var prefix = "//";
    if (!label.startsWith(prefix)) {
      throw new IllegalArgumentException(String.format("%s didn't start with %s", label, prefix));
    }
    var labelWithoutPrefix = label.substring(prefix.length());
    var parts = labelWithoutPrefix.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException(
          String.format("Label %s didn't contain exactly one ':'", label));
    }
    return parts[0];
  }
}
