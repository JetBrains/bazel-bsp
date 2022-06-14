package org.jetbrains.bsp.bazel.server.bloop;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;

class SourceSetRewriter {
  private static final SourceSet EMPTY_SOURCE_SET = new SourceSet(HashSet.empty(), HashSet.empty());

  private final Set<Path> ignoredSources;

  public SourceSetRewriter(Set<Path> ignoredSources) {
    this.ignoredSources = ignoredSources;
  }

  public SourceSet rewrite(SourceSet input) {
    if (input.sources().size() == 1) {
      var singleSource = Paths.get(input.sources().head());
      if (ignoredSources.exists(singleSource::endsWith)) {
        return EMPTY_SOURCE_SET;
      }
    }
    return input;
  }
}
