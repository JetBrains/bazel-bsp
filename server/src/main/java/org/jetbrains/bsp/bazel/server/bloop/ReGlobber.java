package org.jetbrains.bsp.bazel.server.bloop;

import static org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.emptyList;
import static org.jetbrains.bsp.bazel.server.bloop.ScalaInterop.toList;

import bloop.config.Config;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;
import scala.collection.immutable.List;

class ReGlobber {
  public static ReGlobbed reGlob(URI baseDirectory, SourceSet sourceSet) {
    var sourceGlobs = reGlobImpl(baseDirectory, sourceSet.sources());
    if (sourceGlobs.isDefined()) {
      return new ReGlobbed(sourceGlobs, emptyList());
    } else {
      return new ReGlobbed(sourceGlobs, toList(sourceSet.sources().map(Paths::get)));
    }
  }

  private static Option<List<Config.SourcesGlobs>> reGlobImpl(URI baseDirectory, Set<URI> sources) {
    var basePath = Paths.get(baseDirectory);
    var sourcePaths = sources.map(Paths::get);
    var relativeLevels = 0;
    var extensions = new java.util.HashSet<String>();

    for (var s : sourcePaths) {
      var rel = basePath.relativize(s);
      if (!rel.startsWith("..")) {
        relativeLevels = Math.max(relativeLevels, rel.getNameCount());
        var maybeExtension = com.google.common.io.Files.getFileExtension(s.toString());
        if (!maybeExtension.isEmpty()) {
          extensions.add(maybeExtension);
        }
      }
    }
    if (relativeLevels == 0) {
      return Option.none();
    } else {
      scala.Option<Object> walkDepth;
      String globPrefix;
      if (relativeLevels == 1) {
        walkDepth = scala.Option.apply(1);
        globPrefix = "glob:*.";
      } else {
        walkDepth = scala.Option.empty();
        globPrefix = "glob:**.";
      }
      var includes = extensions.stream().map(ext -> globPrefix + ext).iterator();
      var singleGlob = new Config.SourcesGlobs(basePath, walkDepth, toList(includes), emptyList());
      return Option.of(toList(io.vavr.collection.List.of(singleGlob)));
    }
  }

  static final class ReGlobbed {
    public final Option<List<Config.SourcesGlobs>> globs;
    public final List<Path> sources;

    private ReGlobbed(Option<List<Config.SourcesGlobs>> globs, List<Path> sources) {
      this.globs = globs;
      this.sources = sources;
    }
  }
}
