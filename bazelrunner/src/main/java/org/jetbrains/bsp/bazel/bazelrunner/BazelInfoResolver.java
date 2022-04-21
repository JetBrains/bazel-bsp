package org.jetbrains.bsp.bazel.bazelrunner;

import io.vavr.Lazy;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;

public class BazelInfoResolver {
  private static final Pattern INFO_LINE_PATTERN = Pattern.compile("([\\w-]+): (.*)");
  private final BazelRunner bazelRunner;
  private final BazelInfoStorage storage;

  public BazelInfoResolver(BazelRunner bazelRunner, BazelInfoStorage storage) {
    this.bazelRunner = bazelRunner;
    this.storage = storage;
  }

  public BazelInfo resolveBazelInfo() {
    return new LazyBazelInfo(Lazy.of(() -> storage.load().getOrElse(this::bazelInfoFromBazel)));
  }

  private BazelInfo bazelInfoFromBazel() {
    var processResult =
        bazelRunner.commandBuilder().info().executeBazelCommand().waitAndGetResult();
    var info = parseBazelInfo(processResult);
    storage.store(info);
    return info;
  }

  private BasicBazelInfo parseBazelInfo(BazelProcessResult bazelProcessResult) {
    var outputMap =
        bazelProcessResult
            .stdoutLines()
            .flatMap(
                line -> {
                  var matcher = INFO_LINE_PATTERN.matcher(line);
                  return Option.when(
                      matcher.matches(), () -> new Tuple2<>(matcher.group(1), matcher.group(2)));
                })
            .toMap(Function.identity());

    var executionRoot = extract(outputMap, "execution_root");
    var workspace = Paths.get(extract(outputMap, "workspace"));
    return new BasicBazelInfo(executionRoot, workspace);
  }

  private String extract(Map<String, String> outputMap, String name) {
    return outputMap
        .get(name)
        .getOrElseThrow(
            () ->
                new RuntimeException(String.format("Failed to resolve %s from bazel info", name)));
  }
}
