package org.jetbrains.bsp.bazel.server.bsp.utils;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class SourceRootGuesserTest {

  private URI input;
  private String expectedOutput;

  public SourceRootGuesserTest(String input, String expected) throws Exception {
    this.input = (new URL(input)).toURI();
    this.expectedOutput = expected;
  }

  @Test
  public void shouldGuessSourceRoots() {
    String output = SourceRootGuesser.getSourcesRoot(input);
    assertEquals(expectedOutput, output);
  }

  @Parameters
  public static Collection<Object> data() {
    return Arrays.asList(
        (Object[])
            new Object[][] {
              {
                "file:///WORKSPACE_ROOT/java_hello/src/main/java/com/hello/Hello.java",
                "/WORKSPACE_ROOT/java_hello/src/main/java"
              },
              {
                "file:///WORKSPACE_ROOT/server/src/test/java/org/jetbrains/bsp/bazel/server/bsp/utils/SourceRootGuesserTest.java",
                "/WORKSPACE_ROOT/server/src/test/java"
              },
              {
                "file:///WORKSPACE_ROOT/src/main/java/org/test/java",
                "/WORKSPACE_ROOT/src/main/java/org/test/java"
              },
              {"file:///WORKSPACE_ROOT/foo/bar", "/WORKSPACE_ROOT/foo"}
            });
  }
}
