package org.jetbrains.bsp.bazel.server.sync.model;

import io.vavr.collection.Set;
import java.net.URI;
import java.util.Objects;

public class SourceSet {
  private final Set<URI> sources;
  private final Set<URI> sourceRoots;

  public SourceSet(Set<URI> sources, Set<URI> sourceRoots) {
    this.sources = sources;
    this.sourceRoots = sourceRoots;
  }

  public Set<URI> sources() {
    return sources;
  }

  public Set<URI> sourceRoots() {
    return sourceRoots;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SourceSet sourceSet = (SourceSet) o;
    return sources.equals(sourceSet.sources) && sourceRoots.equals(sourceSet.sourceRoots);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sources, sourceRoots);
  }
}
