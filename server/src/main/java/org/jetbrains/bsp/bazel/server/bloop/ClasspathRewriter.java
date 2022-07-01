package org.jetbrains.bsp.bazel.server.bloop;

import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

class ClasspathRewriter {
  private final Map<URI, URI> localArtifacts;

  public ClasspathRewriter(Map<URI, URI> localArtifacts) {
    this.localArtifacts = localArtifacts;
  }

  public Seq<Path> rewrite(Seq<URI> input) {
    return input
        .iterator()
        .map(p -> this.localArtifacts.getOrElse(p, p))
        .distinct()
        .map(Paths::get)
        .toArray();
  }
}
