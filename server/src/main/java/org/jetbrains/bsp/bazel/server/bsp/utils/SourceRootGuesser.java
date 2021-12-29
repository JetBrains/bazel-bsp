package org.jetbrains.bsp.bazel.server.bsp.utils;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class SourceRootGuesser {

  public static String getSourcesRoot(URI sourceUri) {
    Path sourcePath = Paths.get(sourceUri);
    FileSystem fs = FileSystems.getDefault();
    PathMatcher sourceRootPattern =
        fs.getPathMatcher(
            "glob:**/"
                + "{main,test,tests,src,3rdparty,3rd_party,thirdparty,third_party}/"
                + "{resources,scala,java,kotlin,jvm,proto,python,protobuf,py}");
    PathMatcher defaultTestRootPattern = fs.getPathMatcher("glob:**/{test,tests}");
    Path sourceRootGuess = null;
    for (PathMatcher pattern : new PathMatcher[] {sourceRootPattern, defaultTestRootPattern}) {
      sourceRootGuess = approximateSourceRoot(sourcePath, pattern);
      if (sourceRootGuess != null) {
        break;
      }
    }
    if (sourceRootGuess == null) {
      return sourcePath.getParent().toString();
    }
    return sourceRootGuess.toAbsolutePath().toString();
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
