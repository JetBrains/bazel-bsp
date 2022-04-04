package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.List;
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
    return bazelInfo.workspaceRoot().toAbsolutePath().toUri();
  }

  public List<URI> resolveUris(java.util.List<FileLocation> fileLocations) {
    return List.ofAll(fileLocations).map(this::resolveUri);
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
        bazelInfo.execRoot(),
        fileLocation.getRootExecutionPathFragment(),
        fileLocation.getRelativePath());
  }

  private Path resolveSource(FileLocation fileLocation) {
    return bazelInfo.workspaceRoot().resolve(fileLocation.getRelativePath());
  }

  private boolean isMainWorkspaceSource(FileLocation fileLocation) {
    return fileLocation.getIsSource() && !fileLocation.getIsExternal();
  }

  public Path labelToDirectory(Label label) {
    var relativePath = extractRelativePath(label.getValue());
    return bazelInfo.workspaceRoot().resolve(relativePath);
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
