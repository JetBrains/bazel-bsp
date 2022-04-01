package org.jetbrains.bsp.bazel.server.bsp.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SourceRootGuesserTest {

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            // given
            "file:///WORKSPACE_ROOT/java_hello/src/main/java/com/hello/Hello.java",
            // then
            "/WORKSPACE_ROOT/java_hello/src/main/java"),
        Arguments.of(
            // given
            "file:///WORKSPACE_ROOT/server/src/test/java/org/jetbrains/bsp/bazel/server/bsp/utils/SourceRootGuesserTest.java",
            // then
            "/WORKSPACE_ROOT/server/src/test/java"),
        Arguments.of(
            // given
            "file:///WORKSPACE_ROOT/src/main/java/org/test/java",
            // then
            "/WORKSPACE_ROOT/src/main/java/org/test/java"),
        Arguments.of(
            // given
            "file:///WORKSPACE_ROOT/foo/bar",
            // then
            "/WORKSPACE_ROOT/foo"));
  }

  @MethodSource("data")
  @ParameterizedTest(name = "{index}: SourceRootGuesser.getSourcesRoot({0}) should equals {1}")
  public void shouldGuessSourceRoots(String input, String expectedOutput) {
    // when
    String output = SourceRootGuesser.getSourcesRoot(URI.create(input));

    // then
    assertThat(output).isEqualTo(expectedOutput);
  }
}
