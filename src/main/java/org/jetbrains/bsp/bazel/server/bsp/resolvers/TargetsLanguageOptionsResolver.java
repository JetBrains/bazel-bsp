package org.jetbrains.bsp.bazel.server.bsp.resolvers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.commons.Uri;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph.ActionGraphParser;

public class TargetsLanguageOptionsResolver<T> {

  private static final List<String> ACTION_GRAPH_SUFFIXES = ImmutableList.of(".jar", ".js");

  private final BazelData bazelData;
  private final BazelRunner bazelRunner;
  private final ActionGraphResolver actionGraphResolver;
  private final String compilerOptionsName;
  private final List<String> languagesIds;
  private final ResultItemsCollector<T> resultItemsCollector;

  private TargetsLanguageOptionsResolver(
      BazelData bazelData,
      BazelRunner bazelRunner,
      String compilerOptionsName,
      List<String> languagesIds,
      ResultItemsCollector<T> resultItemsCollector) {
    this.bazelData = bazelData;
    this.bazelRunner = bazelRunner;
    this.compilerOptionsName = compilerOptionsName;
    this.languagesIds = languagesIds;
    this.resultItemsCollector = resultItemsCollector;

    this.actionGraphResolver = new ActionGraphResolver(bazelRunner, bazelData);
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public List<T> getResultItemsForTargets(List<BuildTargetIdentifier> buildTargetsIdentifiers) {
    List<String> targets = TargetsUtils.getTargetsUris(buildTargetsIdentifiers);
    ActionGraphParser actionGraphParser =
        actionGraphResolver.getActionGraphParser(targets, languagesIds);

    return targets.stream()
        .flatMap(target -> getResultItems(target, targets, actionGraphParser))
        .collect(Collectors.toList());
  }

  private Stream<T> getResultItems(
      String target, List<String> allTargets, ActionGraphParser actionGraphParser) {
    Map<String, List<String>> targetsOptions = getTargetsOptions(allTargets);

    return getResultItemForActionGraphParserOptionsTargetsOptionsAndTarget(
        actionGraphParser, targetsOptions, target);
  }

  private Map<String, List<String>> getTargetsOptions(List<String> targets) {
    BazelProcess bazelProcess = queryBazel(targets);

    Build.QueryResult query = QueryResolver.getQueryResultForProcess(bazelProcess);

    return query.getTargetList().stream()
        .map(Build.Target::getRule)
        .collect(Collectors.toMap(Build.Rule::getName, this::collectRules));
  }

  private BazelProcess queryBazel(List<String> targets) {
    return bazelRunner
        .commandBuilder()
        .query()
        .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
        .withTargets(targets)
        .executeBazelBesCommand();
  }

  private List<String> collectRules(Build.Rule rule) {
    return rule.getAttributeList().stream()
        .filter(this::isAttributeCompilerOptionsName)
        .flatMap(attr -> attr.getStringListValueList().stream())
        .collect(Collectors.toList());
  }

  private boolean isAttributeCompilerOptionsName(Build.Attribute attribute) {
    return attribute.getName().equals(compilerOptionsName);
  }

  private Stream<T> getResultItemForActionGraphParserOptionsTargetsOptionsAndTarget(
      ActionGraphParser actionGraphParser,
      Map<String, List<String>> targetsOptions,
      String target) {

    BuildTargetIdentifier targetIdentifier = new BuildTargetIdentifier(target);
    List<String> options = targetsOptions.getOrDefault(target, ImmutableList.of());
    List<String> inputs = actionGraphParser.getInputsAsUri(target, bazelData.getExecRoot());

    return actionGraphParser.getOutputs(target, ACTION_GRAPH_SUFFIXES).stream()
        .map(this::mapActionGraphOutputsToClassDirectory)
        .map(
            classDirectory ->
                resultItemsCollector.apply(targetIdentifier, options, inputs, classDirectory));
  }

  private String mapActionGraphOutputsToClassDirectory(String output) {
    String execPath = Constants.EXEC_ROOT_PREFIX + output;

    return Uri.fromExecPath(execPath, bazelData.getExecRoot()).toString();
  }

  @FunctionalInterface
  public interface ResultItemsCollector<T> {

    T apply(
        BuildTargetIdentifier target,
        List<String> options,
        List<String> classpath,
        String classDirectory);
  }

  public static class Builder<T> {

    private BazelData bazelData;
    private BazelRunner bazelRunner;
    private String compilerOptionsName;
    private List<String> languagesIds;
    private ResultItemsCollector<T> resultItemsCollector;

    public Builder<T> bazelData(BazelData bazelData) {
      this.bazelData = bazelData;
      return this;
    }

    public Builder<T> bazelRunner(BazelRunner bazelRunner) {
      this.bazelRunner = bazelRunner;
      return this;
    }

    public Builder<T> compilerOptionsName(String compilerOptionsName) {
      this.compilerOptionsName = compilerOptionsName;
      return this;
    }

    public Builder<T> languagesIds(List<String> languagesIds) {
      this.languagesIds = languagesIds;
      return this;
    }

    public Builder<T> resultItemsCollector(ResultItemsCollector<T> resultItemsCollector) {
      this.resultItemsCollector = resultItemsCollector;
      return this;
    }

    public TargetsLanguageOptionsResolver<T> build() {
      throwExceptionIfAnyFieldIsNotFilled();

      return new TargetsLanguageOptionsResolver<T>(
          bazelData, bazelRunner, compilerOptionsName, languagesIds, resultItemsCollector);
    }

    private void throwExceptionIfAnyFieldIsNotFilled() {
      if (bazelData == null
          || bazelRunner == null
          || compilerOptionsName == null
          || languagesIds == null
          || resultItemsCollector == null) {
        throw new IllegalStateException("Every TargetsResolver.Builder field has to be set");
      }
    }
  }
}
