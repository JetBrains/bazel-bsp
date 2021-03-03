package org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph;

import com.google.common.collect.Lists;
import org.jetbrains.bsp.bazel.commons.Uri;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ActionGraphParser {
  abstract public List<String> getOutputs(String target, List<String> suffixes);

  abstract protected Stream<String> buildInputs(String target, List<String> suffixes);

  public static final String EXEC_ROOT = "exec-root://";

  protected List<String> getInputs(String target, List<String> suffixes){
    return buildInputs(target, suffixes)
            .filter(path -> suffixes.stream().anyMatch(path::endsWith))
            .distinct()
            .collect(Collectors.toList());
  }

  public List<String> getInputsAsUri(String target, String execRoot) {
    return getInputs(target, Lists.newArrayList(".jar", "js")).stream()
            .map(execPath -> Uri.fromExecPath(execPath, execRoot).toString())
            .collect(Collectors.toList());
  }
}
