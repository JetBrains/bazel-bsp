package org.jetbrains.bsp.bazel.server.bsp.utils;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SourceRootGuesser {

  public static Path getSourcesRoot(Path sourcePath) {
    FileSystem fs = FileSystems.getDefault();
    PathMatcher sourceRootPattern =
        fs.getPathMatcher(
            "glob:**/"
                + "{main,test,tests,src,3rdparty,3rd_party,thirdparty,third_party}/"
                + "{*resources,scala,java,kotlin,jvm,proto,python,protobuf,py}");
    PathMatcher defaultTestRootPattern = fs.getPathMatcher("glob:**/{test,tests}");

    Optional<Path> sourceRootGuess =
        Stream.of(sourceRootPattern, defaultTestRootPattern)
            .map(pattern -> approximateSourceRoot(sourcePath, pattern))
            .filter(Objects::nonNull)
            .findFirst();

    return sourceRootGuess.orElse(sourcePath.getParent()).toAbsolutePath();
  }

  public static String getSourcesRoot(URI sourceUri) {
    return getSourcesRoot(Paths.get(sourceUri)).toString();
  }

  private static Path approximateSourceRoot(Path dir, PathMatcher matcher) {
    Path guess = dir;
    while (guess != null) {
      if (matcher.matches(guess)) {
        return guess;
      } else {
        guess = guess.getParent();
      }
    }
    return null;
  }
}
