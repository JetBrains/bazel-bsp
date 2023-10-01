package org.jetbrains.bsp.bazel.commons;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;

public class Format {
  public static String duration(Duration duration) {
    if (duration.toSeconds() == 0) {
      return duration.toMillis() + "ms";
    }
    if (duration.toMinutes() == 0) {
      var df = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
      return df.format(((double) duration.toMillis()) / 1000) + "s";
    }
    var minutes = duration.toMinutes() + "m";

    if (duration.toSecondsPart() > 0 || duration.toMillisPart() > 0) {
      var millisAsSeconds = ((double) duration.toMillisPart()) / 1000;
      var seconds = millisAsSeconds + duration.toSecondsPart();
      return minutes + " " + (Math.round(seconds) + "s");
    }

    return minutes;
  }
}
