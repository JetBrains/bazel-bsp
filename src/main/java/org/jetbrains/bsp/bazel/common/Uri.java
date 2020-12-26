package org.jetbrains.bsp.bazel.common;

import com.google.common.base.Splitter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

public class Uri implements Comparable<Uri> {

  private static final String ENC = StandardCharsets.UTF_8.toString();
  private final URI uri;

  private Uri(String uri) {
    this.uri = URI.create(uri);
  }

  private Uri(URI uri) {
    this.uri = uri;
  }

  public static Uri fromFileUri(String uri) {
    return new Uri(uri);
  }

  public static Uri fromAbsolutePath(String path) {
    return new Uri(Paths.get(path).toUri());
  }

  public static Uri fromExecPath(String execPath, String execRoot) {
    String prefix = "exec-root://";
    if (!execPath.startsWith(prefix)) {
      throw new IllegalArgumentException(
          String.format("%s didn't start with %s", execPath, prefix));
    }
    String path = String.format("/%s/%s", execRoot, execPath.substring(prefix.length()));
    return fromAbsolutePath(path);
  }

  public static Uri fromFileLabel(String fileLabel, String workspaceRoot) {
    List<String> parts = divideFileLabel(fileLabel);
    String path = String.format("/%s/%s/%s", workspaceRoot, parts.get(0), parts.get(1));
    return fromAbsolutePath(path);
  }

  public static Uri packageDirFromLabel(String fileLabel, String workspaceRoot) {
    List<String> parts = divideFileLabel(fileLabel);
    String path = String.format("/%s/%s", workspaceRoot, parts.get(0));
    return fromAbsolutePath(path);
  }

  private static List<String> divideFileLabel(String fileLabel) {
    if (!fileLabel.startsWith("//")) {
      throw new IllegalArgumentException(String.format("%s didn't start with %s", fileLabel, "//"));
    }
    String fileLabelWithoutPrefix = fileLabel.substring(2);
    List<String> parts = Splitter.on(':').splitToList(fileLabelWithoutPrefix);
    if (parts.size() != 2) {
      throw new IllegalArgumentException(
          String.format("Label %s didn't contain exactly one :", fileLabel));
    }
    return parts;
  }

  public static Uri fromWorkspacePath(String path, String workspaceRoot) {
    System.out.println("From workspace path: " + path);
    String fullPath = String.format("/%s/%s", workspaceRoot, path);
    return fromAbsolutePath(fullPath);
  }

  public static Uri fromExecOrWorkspacePath(String path, String execRoot, String workspaceRoot) {
    if (path.startsWith("exec-root://")) {
      return fromExecPath(path, execRoot);
    } else if (path.startsWith("workspace-root://")) {
      return fromWorkspacePath(path.substring("workspace-root://".length()), workspaceRoot);
    } else if (path.contains("execroot/__main__")) {
      return fromWorkspacePath(path.substring(execRoot.length()), workspaceRoot);
    } else {
      return fromWorkspacePath(path, workspaceRoot);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof Uri)) {
      return false;
    }
    return uri.equals(((Uri) o).uri);
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return uri.toString();
  }

  @Override
  public int compareTo(Uri other) {
    return this.uri.compareTo(other.uri);
  }
}
