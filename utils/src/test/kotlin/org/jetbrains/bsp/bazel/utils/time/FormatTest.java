package org.jetbrains.bsp.bazel.commons;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class FormatTest {

  @Test
  public void formatMillis() {
    test(Duration.ofMillis(100), "100ms");
  }

  @Test
  public void formatMillisPrecise() {
    test(Duration.ofMillis(153), "153ms");
  }

  @Test
  public void formatSeconds() {
    test(Duration.ofMillis(1000), "1s");
  }

  @Test
  public void formatSecondsAndMillis() {
    test(Duration.ofMillis(1500), "1.5s");
  }

  @Test
  public void formatSecondsAndMillisLowerRounding() {
    test(Duration.ofMillis(1502), "1.5s");
  }

  @Test
  public void formatSecondsAndMillisUpperRounding() {
    test(Duration.ofMillis(1552), "1.6s");
  }

  @Test
  public void formatMinutes() {
    test(Duration.ofMinutes(3), "3m");
  }

  @Test
  public void formatMinutesAndSeconds() {
    test(Duration.ofSeconds(90), "1m 30s");
  }

  @Test
  public void formatMinutesAndSecondsAndMillis() {
    test(Duration.ofMillis(90 * 1000 + 501), "1m 31s");
  }

  private void test(Duration duration, String expected) {
    assertThat(Format.duration(duration)).isEqualTo(expected);
  }
}
