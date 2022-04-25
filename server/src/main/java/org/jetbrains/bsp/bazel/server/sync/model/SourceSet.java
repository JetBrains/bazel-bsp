package org.jetbrains.bsp.bazel.server.sync.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.Set;
import java.nio.file.Path;
import java.util.Objects;
import org.jetbrains.bsp.bazel.commons.Format;

public class SourceSet {
  private final Set<Path> sources;
  private final Set<Path> sourceRoots;

  public SourceSet(
      @JsonProperty("sources") Set<Path> sources,
      @JsonProperty("sourceRoots") Set<Path> sourceRoots) {
    this.sources = sources;
    this.sourceRoots = sourceRoots;
  }

  public Set<Path> sources() {
    return sources;
  }

  public Set<Path> sourceRoots() {
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

  @Override
  public String toString() {
    return Format.object(
        "SourceSet",
        Format.entry("sources", Format.iterable(sources)),
        Format.entry("sourceRoots", Format.iterable(sourceRoots)));
  }
}
