package org.jetbrains.bsp.bazel.server.bsp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.bazelrunner.BazelData;
import org.jetbrains.bsp.bazel.bazelrunner.SemanticVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class InternalAspectsResolverTest {

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            // given
            createBazelData(
                "/Users/user/workspace/test-project", "/Users/user/workspace/test-project"),
            "get_classpath",
            // then
            "@//.bazelbsp:aspects.bzl%get_classpath"),
        Arguments.of(
            // given
            createBazelData(
                "/Users/user/workspace/test-project",
                "/Users/user/workspace/test-project/bsp-projects/test-project-bsp"),
            "get_classpath",
            // then
            "@//bsp-projects/test-project-bsp/.bazelbsp:aspects.bzl%get_classpath"));
  }

  private static BazelData createBazelData(String workspaceRoot, String bspRoot) {
    return new BazelData() {
      @Override
      public String getExecRoot() {
        return null;
      }

      @Override
      public String getWorkspaceRoot() {
        return workspaceRoot;
      }

      @Override
      public String getBinRoot() {
        return null;
      }

      @Override
      public SemanticVersion getVersion() {
        return null;
      }

      @Override
      public Path getBspProjectRoot() {
        return Path.of(bspRoot);
      }
    };
  }

  @MethodSource("data")
  @ParameterizedTest(name = "{index}: internalAspectsResolver.resolveLabel({1}) should equals {2}")
  public void shouldResolveLabels(
      BazelData bazelData, String aspectName, String expectedAspectLabel) {
    // when
    var internalAspectsResolver = new InternalAspectsResolver(bazelData);
    var aspectLabel = internalAspectsResolver.resolveLabel(aspectName);

    // then
    assertThat(aspectLabel).isEqualTo(expectedAspectLabel);
  }
}
