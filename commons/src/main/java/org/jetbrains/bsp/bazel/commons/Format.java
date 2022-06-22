package org.jetbrains.bsp.bazel.commons;

import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;
import java.util.stream.Collectors;

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

  public static <T> String iterable(Traversable<T> xs) {
    if (xs.isEmpty()) {
      return "[]";
    }
    return xs.map(x -> indent(x.toString())).mkString("[\n", ",\n", "\n]");
  }

  public static <T> String iterable(java.util.stream.Stream<T> c) {
    if (c.findAny().isPresent()) {
      return "[]";
    }
    return c.map(o -> indent(String.valueOf(o))).collect(Collectors.joining(",\n", "[\n", "\n]"));
  }

  public static <T> String iterableShort(Traversable<T> xs) {
    return xs.mkString("[", ", ", "]");
  }

  public static <T> String iterableShort(java.util.stream.Stream<T> c) {
    return c.map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
  }

  public static String object(String name, String... entries) {
    return Stream.of(entries).map(Format::indent).mkString(name + " {\n", "\n", "\n}");
  }

  public static String entry(String name, Object value) {
    return name + " = " + value;
  }

  private static String indent(String s) {
    return Stream.ofAll(s.lines()).map(l -> "  " + l).mkString("\n");
  }
}
