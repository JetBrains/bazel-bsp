package org.jetbrains.bsp.bazel.server.bloop;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClasspathRewriter {

  private final Map<URI, URI> localArtifacts;

  public ClasspathRewriter(Map<URI, URI> localArtifacts) {
    this.localArtifacts = localArtifacts;
  }

  public List<Path> rewrite(List<URI> input) {
    return input.stream()
        .map(p -> this.localArtifacts.getOrDefault(p, p))
        .distinct()
        .map(Paths::get)
        .collect(Collectors.toList());
  }
}
