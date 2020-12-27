package org.jetbrains.bsp.bazel.server.resolvers;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetsResolver {

  private final QueryResolver queryResolver;

  private static final String MAIN_CLASS_ATTR_NAME = "main_class";

  public TargetsResolver(QueryResolver queryResolver) {
    this.queryResolver = queryResolver;
  }

  public Map<String, List<String>> getTargetsOptions(
      String targetsUnion, String compilerOptionsName) {
    Build.QueryResult query =
        queryResolver.getQuery("query", "--output=proto", "(" + targetsUnion + ")");

    return query.getTargetList().stream()
        .map(Build.Target::getRule)
        .collect(
            Collectors.toMap(
                Build.Rule::getName,
                (rule) ->
                    getAttribute(rule, compilerOptionsName)
                        .flatMap(attr -> attr.getStringListValueList().stream())
                        .collect(Collectors.toList())));
  }

  public Map<String, List<String>> getTargetsMainClasses(String targetsUnion) {
    Build.QueryResult query =
        queryResolver.getQuery("query", "--output=proto", "(" + targetsUnion + ")");

    return query.getTargetList().stream()
        .map(Build.Target::getRule)
        .collect(
            Collectors.toMap(
                Build.Rule::getName,
                (rule) ->
                    getAttribute(rule, MAIN_CLASS_ATTR_NAME)
                        .map(Build.Attribute::getStringValue)
                        .collect(Collectors.toList())));
  }

  Stream<Build.Attribute> getAttribute(Build.Rule rule, String name) {
    return rule.getAttributeList().stream()
        .filter(
            attr ->
                attr.getName().equals(name)
                    && attr.hasExplicitlySpecified()
                    && attr.getExplicitlySpecified());
  }
}
