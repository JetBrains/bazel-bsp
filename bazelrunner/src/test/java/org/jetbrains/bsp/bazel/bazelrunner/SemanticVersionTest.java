package org.jetbrains.bsp.bazel.bazelrunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SemanticVersionTest {

  @Test
  public void shouldThrowForNull() {
    assertThrows(IllegalArgumentException.class, () -> new SemanticVersion(null));
  }

  @Test
  public void shouldThrowForTooShortVersion() {
    assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("1.2"));
  }

  @Test
  public void shouldThrowForTooLongVersion() {
    assertThrows(IllegalArgumentException.class, () -> new SemanticVersion("1.2.3.4.5"));
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

  @Test
  public void shouldParseRollingRelease() {
    SemanticVersion version = new SemanticVersion("5.0.0-pre.20210817.2");

    assertEquals(5, version.getMajorVersion());
    assertEquals(0, version.getMinorVersion());
    assertEquals(0, version.getPatchVersion());
  }
}
