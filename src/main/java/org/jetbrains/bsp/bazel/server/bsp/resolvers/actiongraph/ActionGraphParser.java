package org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.bsp.bazel.commons.Uri;

public abstract class ActionGraphParser {

  // Obtains all of the files outputted by this target that match a list of suffixes
  public abstract List<String> getOutputs(String target, List<String> suffixes);

  // Obtains all of the dependencies, or inputs, of this target that match a list of suffixes
  protected abstract Stream<String> buildInputs(String target, List<String> suffixes);

  public static final String EXEC_ROOT = "exec-root://";

  protected List<String> getInputs(String target, List<String> suffixes) {
    return buildInputs(target, suffixes)
        .filter(path -> suffixes.stream().anyMatch(path::endsWith))
        .distinct()
        .collect(Collectors.toList());
  }

  // Obtains all of the dependencies, or inputs, of this target and converts them to Uri form
  public List<String> getInputsAsUri(String target, String execRoot) {
    return getInputs(target, Lists.newArrayList(".jar", "js")).stream()
        .map(execPath -> Uri.fromExecPath(execPath, execRoot).toString())
        .collect(Collectors.toList());
  }
}
