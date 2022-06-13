package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation;
import org.jetbrains.bsp.bazel.server.sync.model.Label;

public class BazelPathsResolver {
  private final BazelInfo bazelInfo;

  private final ConcurrentHashMap<Path, URI> uris = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<FileLocation, Path> paths = new ConcurrentHashMap<>();

  public BazelPathsResolver(BazelInfo bazelInfo) {
    this.bazelInfo = bazelInfo;
  }

  public URI resolveUri(Path path) {
    return uris.computeIfAbsent(path, Path::toUri);
  }

  public URI workspaceRoot() {
    return resolveUri(bazelInfo.getWorkspaceRoot().toAbsolutePath());
  }

  public Seq<URI> resolveUris(java.util.List<FileLocation> fileLocations) {
    return fileLocations.stream()
        .map(this::resolve)
        .map(this::resolveUri)
        .collect(Array.collector());
  }

  public Seq<Path> resolvePaths(java.util.List<FileLocation> fileLocations) {
    return fileLocations.stream().map(this::resolve).collect(Array.collector());
  }

  public URI resolveUri(FileLocation fileLocation) {
    return resolveUri(resolve(fileLocation));
  }

  public Path resolve(FileLocation fileLocation) {
    return paths.computeIfAbsent(fileLocation, this::doResolve);
  }

  private Path doResolve(FileLocation fileLocation) {
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

  public URI labelToDirectoryUri(Label label) {
    var relativePath = extractRelativePath(label.getValue());
    return resolveUri(bazelInfo.getWorkspaceRoot().resolve(relativePath));
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
