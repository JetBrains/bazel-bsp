package org.jetbrains.bsp.bazel.server.resolvers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.base.Joiner;
import com.google.devtools.build.lib.query2.proto.proto2api.Build.QueryResult;
import com.google.devtools.build.lib.query2.proto.proto2api.Build.Rule;
import com.google.devtools.build.lib.query2.proto.proto2api.Build.Target;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TargetsResolver {

  private static final String TARGETS_UNION_SEPARATOR = " + ";
  private static final String JAVA_TARGET_OPTIONS = "javacopts";
  private static final String SCALA_TARGET_OPTIONS = "scalacopts";

  private final BazelQueryRunner bazelQueryRunner;

  public TargetsResolver(BazelQueryRunner bazelQueryRunner) {
    this.bazelQueryRunner = bazelQueryRunner;
  }

  public List<String> getTargetsUris(List<BuildTargetIdentifier> buildTargetIdentifiers) {
    return buildTargetIdentifiers.stream()
        .map(BuildTargetIdentifier::getUri)
        .collect(Collectors.toList());
  }

  public Map<String, List<String>> getJavacTargetsOptions(List<String> targets) {
    return getTargetsOptions(targets, JAVA_TARGET_OPTIONS);
  }

  public Map<String, List<String>> getScalacTargetsOptions(List<String> targets) {
    return getTargetsOptions(targets, SCALA_TARGET_OPTIONS);
  }

  private Map<String, List<String>> getTargetsOptions(List<String> targets, String compilerOptionsName) {
    String targetsUnion = Joiner.on(TARGETS_UNION_SEPARATOR).join(targets);

    QueryResult query =
        bazelQueryRunner.queryWithProtobufOutput("(" + targetsUnion + ")");

    return getTargetsOptionsAsMap(compilerOptionsName, query);
  }

  private Map<String, List<String>> getTargetsOptionsAsMap(String compilerOptionsName, QueryResult query) {
    return query.getTargetList().stream()
        .map(Target::getRule)
        .collect(
            Collectors.toMap(
                Rule::getName,
                rule -> collectRuleAttributes(compilerOptionsName, rule)));
  }

  private List<String> collectRuleAttributes(String compilerOptionsName, Rule rule) {
    return rule.getAttributeList().stream()
        .filter(attr -> attr.getName().equals(compilerOptionsName))
        .flatMap(attr -> attr.getStringListValueList().stream())
        .collect(Collectors.toList());
  }
}
