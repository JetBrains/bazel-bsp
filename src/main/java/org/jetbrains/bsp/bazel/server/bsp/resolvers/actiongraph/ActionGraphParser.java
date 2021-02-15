package org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph;

import java.util.List;

public interface ActionGraphParser {
    List<String> getInputsAsUri(String target, String execRoot);

    List<String> getOutputs(String target, List<String> suffixes);
}
