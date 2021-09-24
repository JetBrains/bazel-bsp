package org.jetbrains.bsp.bazel.server.bazel.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SemanticVersionTest {

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowForNull() {
    new SemanticVersion(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowForTooShortVersion() {
    new SemanticVersion("1.2");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowForTooLongVersion() {
    new SemanticVersion("1.2.3.4.5");
  }

  @Test
  public void shouldParseSimpleVersion() {
    SemanticVersion version = new SemanticVersion("1.2.3");

    assertEquals(1, version.getMajorVersion());
    assertEquals(2, version.getMinorVersion());
    assertEquals(3, version.getPatchVersion());
  }

  @Test
  public void shouldParseHomebrewVersion() {
    SemanticVersion version = new SemanticVersion("1.2.3-homebrew");

    assertEquals(1, version.getMajorVersion());
    assertEquals(2, version.getMinorVersion());
    assertEquals(3, version.getPatchVersion());
  }
}
