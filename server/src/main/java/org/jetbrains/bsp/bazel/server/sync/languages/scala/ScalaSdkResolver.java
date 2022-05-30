package org.jetbrains.bsp.bazel.server.sync.languages.scala;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Pattern;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;

public class ScalaSdkResolver {
  private static final Comparator<String> SCALA_VERSION_COMPARATOR =
      (a, b) -> {
        var aParts = a.split("\\.");
        var bParts = b.split("\\.");
        for (int i = 0; i < Math.min(aParts.length, bParts.length); i++) {
          var result = Integer.compare(Integer.parseInt(aParts[i]), Integer.parseInt(bParts[i]));
          if (result != 0) return result;
        }
        return 0;
      };
  private final BazelPathsResolver bazelPathsResolver;
  private final Pattern VERSION_PATTERN =
      Pattern.compile("scala-(?:library|compiler|reflect)-([.\\d]+)\\.jar");

  public ScalaSdkResolver(BazelPathsResolver bazelPathsResolver) {
    this.bazelPathsResolver = bazelPathsResolver;
  }

  public Option<ScalaSdk> resolve(Seq<TargetInfo> targets) {
    return targets
        .flatMap(this::resolveSdk)
        .distinct()
        .sortBy(SCALA_VERSION_COMPARATOR, ScalaSdk::version)
        .lastOption();
  }

  private Option<ScalaSdk> resolveSdk(TargetInfo targetInfo) {
    if (!targetInfo.hasScalaToolchainInfo()) {
      return Option.none();
    }

    var scalaToolchain = targetInfo.getScalaToolchainInfo();
    var compilerJars =
        bazelPathsResolver.resolvePaths(scalaToolchain.getCompilerClasspathList()).sorted();
    var maybeVersions = compilerJars.flatMap(this::extractVersion);
    if (maybeVersions.isEmpty()) {
      return Option.none();
    }
    var version = maybeVersions.distinct().sorted().last();
    var binaryVersion = toBinaryVersion(version);
    var sdk = new ScalaSdk("org.scala-lang", version, binaryVersion, compilerJars.map(Path::toUri));
    return Option.some(sdk);
  }

  private Option<String> extractVersion(Path path) {
    var name = path.getFileName().toString();
    var matcher = VERSION_PATTERN.matcher(name);
    if (matcher.matches()) {
      return Option.some(matcher.group(1));
    }
    return Option.none();
  }

  private String toBinaryVersion(String version) {
    return Array.of(version.split("\\.")).take(2).mkString(".");
  }
}
