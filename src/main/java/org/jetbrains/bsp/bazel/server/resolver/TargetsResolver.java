package org.jetbrains.bsp.bazel.server.resolver;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TargetsResolver {

  private final QueryResolver queryResolver;

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
                    rule.getAttributeList().stream()
                        .filter(attr -> attr.getName().equals(compilerOptionsName))
                        .flatMap(attr -> attr.getStringListValueList().stream())
                        .collect(Collectors.toList())));
  }
}
