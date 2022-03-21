package org.jetbrains.bsp.bazel.server.sync.languages.java;

import io.vavr.PartialFunction;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.net.URI;
import java.util.Objects;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;

public class JdkResolver {
  private final BazelPathsResolver bazelPathsResolver;

  public JdkResolver(BazelPathsResolver bazelPathsResolver) {
    this.bazelPathsResolver = bazelPathsResolver;
  }

  public Option<Jdk> resolve(Seq<TargetInfo> targets) {
    var allCandidates = targets.flatMap(this::resolveJdk);
    if (allCandidates.isEmpty()) return Option.none();
    var latestVersion = candidatesWithLatestVersion(allCandidates);
    var complete = allCandidates.filter(JdkCandidate::isComplete);
    var latestVersionAndComplete = latestVersion.filter(JdkCandidate::isComplete);
    return Option.<JdkCandidate>none()
        .orElse(() -> pickCandidateFromJvmRuntime(latestVersionAndComplete))
        .orElse(() -> pickAnyCandidate(latestVersionAndComplete))
        .orElse(() -> pickCandidateFromJvmRuntime(complete))
        .orElse(() -> pickAnyCandidate(complete))
        .orElse(() -> createHybridCandidate(allCandidates))
        .orElse(() -> pickAnyCandidate(allCandidates))
        .flatMap(JdkCandidate::asJdk);
  }

  private Seq<JdkCandidate> candidatesWithLatestVersion(Seq<JdkCandidate> candidates) {
    var latestVersion = findLatestVersion(candidates);
    var byVersion =
        latestVersion.map(
            version -> candidates.filter(candidate -> candidate.version.contains(version)));
    // sorted for deterministic output
    return byVersion.getOrElse(List.empty()).sortBy(c -> c.javaHome.toString());
  }

  private Option<String> findLatestVersion(Seq<JdkCandidate> candidates) {
    return candidates.flatMap(c -> c.version).maxBy(s -> Integer.valueOf(s));
  }

  private Option<JdkCandidate> pickCandidateFromJvmRuntime(Seq<JdkCandidate> candidates) {
    return candidates.find(candidate -> candidate.isRuntime);
  }

  private Option<JdkCandidate> pickAnyCandidate(Seq<JdkCandidate> candidates) {
    return candidates.headOption();
  }

  private Option<JdkCandidate> createHybridCandidate(Seq<JdkCandidate> candidates) {
    var version = findLatestVersion(candidates);
    var javaHome =
        candidates
            .sortBy(c -> !c.isRuntime)
            .collect(PartialFunction.unlift(c -> c.javaHome))
            .headOption();
    return javaHome.map(jh -> new JdkCandidate(true, javaHome, version));
  }

  private Option<JdkCandidate> resolveJdk(TargetInfo targetInfo) {
    var hasRuntimeJavaHome =
        targetInfo.hasJavaRuntimeInfo() && targetInfo.getJavaRuntimeInfo().hasJavaHome();

    var hasToolchainJavaHome =
        targetInfo.hasJavaToolchainInfo() && targetInfo.getJavaToolchainInfo().hasJavaHome();

    var javaHomeFile =
        hasRuntimeJavaHome
            ? (targetInfo.getJavaRuntimeInfo().getJavaHome())
            : (hasToolchainJavaHome ? targetInfo.getJavaToolchainInfo().getJavaHome() : null);

    var javaHome = Option.of(javaHomeFile).map(bazelPathsResolver::resolveUri);

    Option<String> version =
        targetInfo.hasJavaToolchainInfo()
            ? Option.some(targetInfo.getJavaToolchainInfo().getSourceVersion())
            : Option.none();

    return Option.some(new JdkCandidate(hasRuntimeJavaHome, javaHome, version));
  }

  private static class JdkCandidate {
    final boolean isRuntime;
    final Option<URI> javaHome;
    final Option<String> version;

    public JdkCandidate(boolean isRuntime, Option<URI> javaHome, Option<String> version) {
      this.isRuntime = isRuntime;
      this.javaHome = javaHome;
      this.version = version;
    }

    public boolean isComplete() {
      return javaHome.isDefined() && version.isDefined();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      JdkCandidate that = (JdkCandidate) o;
      return isRuntime == that.isRuntime
          && javaHome.equals(that.javaHome)
          && version.equals(that.version);
    }

    @Override
    public int hashCode() {
      return Objects.hash(isRuntime, javaHome, version);
    }

    public Option<Jdk> asJdk() {
      return version.map(ver -> new Jdk(ver, javaHome));
    }
  }
}
