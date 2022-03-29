package org.jetbrains.bsp.bazel.server.bsp.utils;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.bazelrunner.BazelData;
import org.jetbrains.bsp.bazel.bazelrunner.SemanticVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InternalAspectsResolverTest {

  @MethodSource("data")
  @ParameterizedTest
  public void shouldResolveLabels(
      String workspaceRoot, String bspRoot, String aspectName, String expectedAspectLabel) {
    var bazelData = createBazelData(workspaceRoot, bspRoot, aspectName, expectedAspectLabel);
    var actual = new InternalAspectsResolver(bazelData).resolveLabel(aspectName);
    assertEquals(expectedAspectLabel, actual);
  }

  private BazelData createBazelData(
      String workspaceRoot, String bspRoot, String aspectName, String expectedAspectLabel) {
    return new BazelData() {
      @Override
      public String getExecRoot() {
        return null;
      }

      @Override
      public String getWorkspaceRoot() {
        return workspaceRoot.toString();
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

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            "/Users/user/workspace/test-project",
            "/Users/user/workspace/test-project",
            "get_classpath",
            "@//.bazelbsp:aspects.bzl%get_classpath"),
        Arguments.of(
            "/Users/user/workspace/test-project",
            "/Users/user/workspace/test-project/bsp-projects/test-project-bsp",
            "get_classpath",
            "@//bsp-projects/test-project-bsp/.bazelbsp:aspects.bzl%get_classpath"));
  }
}
