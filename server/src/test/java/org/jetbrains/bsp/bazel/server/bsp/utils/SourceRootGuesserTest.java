package org.jetbrains.bsp.bazel.server.bsp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SourceRootGuesserTest {

  @MethodSource("data")
  @ParameterizedTest
  public void shouldGuessSourceRoots(String input, String expectedOutput) {
    String output = SourceRootGuesser.getSourcesRoot(URI.create(input));
    assertEquals(expectedOutput, output);
  }

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            "file:///WORKSPACE_ROOT/java_hello/src/main/java/com/hello/Hello.java",
            "/WORKSPACE_ROOT/java_hello/src/main/java"),
        Arguments.of(
            "file:///WORKSPACE_ROOT/server/src/test/java/org/jetbrains/bsp/bazel/server/bsp/utils/SourceRootGuesserTest.java",
            "/WORKSPACE_ROOT/server/src/test/java"),
        Arguments.of(
            "file:///WORKSPACE_ROOT/src/main/java/org/test/java",
            "/WORKSPACE_ROOT/src/main/java/org/test/java"),
        Arguments.of("file:///WORKSPACE_ROOT/foo/bar", "/WORKSPACE_ROOT/foo"));
  }
}
