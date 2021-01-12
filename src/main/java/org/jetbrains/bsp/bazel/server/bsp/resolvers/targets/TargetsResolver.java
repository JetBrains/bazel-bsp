package org.jetbrains.bsp.bazel.server.bsp.resolvers.targets;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;

public class TargetsResolver {

  private final BazelData bazelData;
  private final BazelRunner bazelRunner;
  private final String compilerOptionsName;

  private TargetsResolver(BazelData bazelData, BazelRunner bazelRunner, String compilerOptionsName) {
    this.bazelData = bazelData;
    this.bazelRunner = bazelRunner;
    this.compilerOptionsName = compilerOptionsName;
  }

  public static class Builder {

    private BazelData bazelData;
    private BazelRunner bazelRunner;
    private String compilerOptionsName;

    public Builder bazelData(BazelData bazelData) {
      this.bazelData = bazelData;
      return this;
    }

    public Builder bazelRunner(BazelRunner bazelRunner) {
      this.bazelRunner = bazelRunner;
      return this;
    }

    public Builder compilerOptionsName(String compilerOptionsName) {
      this.compilerOptionsName = compilerOptionsName;
      return this;
    }

    public TargetsResolver build() {
      throwExceptionIfAnyFieldIsNotFilled();

      return new TargetsResolver(bazelData, bazelRunner, compilerOptionsName);
    }

    private void throwExceptionIfAnyFieldIsNotFilled() {
      if (bazelData == null || bazelRunner == null || compilerOptionsName == null) {
        throw new InvalidParameterException("Every TargetsResolver.Builder field has to be set");
      }
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public Map<String, List<String>> getTargetsOptions(List<String> targets) {
    BazelProcessResult bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withTargets(targets)
            .executeBazelCommand();
    Build.QueryResult query = QueryResolver.getQueryResultForProcess(bazelProcessResult);

    return query.getTargetList().stream()
        .map(Build.Target::getRule)
        .collect(
            Collectors.toMap(
                Build.Rule::getName,
                (rule) ->
                    rule.getAttributeList().stream()
                        .filter(attr -> attr.getName().equals(compilerOptionsName))
                        .flatMap(attr -> attr.getStringListValueList().stream())
                        .collect(Collectors.toList())));
  }
}
