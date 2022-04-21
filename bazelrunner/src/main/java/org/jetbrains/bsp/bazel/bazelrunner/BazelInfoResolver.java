package org.jetbrains.bsp.bazel.bazelrunner;

import io.vavr.Lazy;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.util.function.Function;
import java.util.regex.Pattern;

public class BazelInfoResolver {
  private static final Pattern INFO_LINE_PATTERN = Pattern.compile("([\\w-]+): (.*)");
  private final BazelRunner bazelRunner;

  public BazelInfoResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BazelInfo resolveBazelInfo() {
    return new LazyBazelInfo(Lazy.of(this::readBazelInfoMap));
  }

  private Map<String, String> readBazelInfoMap() {
    var bazelProcessResult =
        bazelRunner.commandBuilder().info().executeBazelCommand().waitAndGetResult();

    return bazelProcessResult
        .stdoutLines()
        .flatMap(
            line -> {
              var matcher = INFO_LINE_PATTERN.matcher(line);
              if (matcher.matches()) {
                return Option.some(new Tuple2<>(matcher.group(1), matcher.group(2)));
              } else {
                return Option.none();
              }
            })
        .toMap(Function.identity());
  }
}
