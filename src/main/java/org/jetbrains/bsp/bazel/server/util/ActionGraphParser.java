package org.jetbrains.bsp.bazel.server.util;

import com.google.common.collect.Lists;
import com.google.devtools.build.lib.analysis.AnalysisProtos;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.common.Uri;

// TODO(illicitonion): Index, cache, etc
public class ActionGraphParser {

  private final AnalysisProtos.ActionGraphContainer actionGraph;

  public ActionGraphParser(AnalysisProtos.ActionGraphContainer actionGraph) {
    this.actionGraph = actionGraph;
  }

  public List<String> getInputs(String target, List<String> suffixes) {
    return getActions(target).stream()
        .flatMap(action -> action.getInputDepSetIdsList().stream())
        .flatMap(
            depset -> {
              Queue<String> queue = new ArrayDeque<>();
              queue.add(depset);
              return expandDepsetToArtifacts(queue).stream();
            })
        .map(artifact -> "exec-root://" + artifact.getExecPath())
        .filter(path -> suffixes.stream().anyMatch(path::endsWith))
        .collect(Collectors.toCollection(TreeSet::new))
        .stream()
        .collect(Collectors.toList());
  }

  public List<String> getInputsAsUri(String target, String execRoot) {
    return getInputs(target, Lists.newArrayList(".jar", "js")).stream()
        .map(exec_path -> Uri.fromExecPath(exec_path, execRoot).toString())
        .collect(Collectors.toList());
  }

  public List<String> getOutputs(String target, List<String> suffixes) {
    Set<String> artifactIds =
        getActions(target).stream()
            .flatMap(action -> action.getOutputIdsList().stream())
            .collect(Collectors.toSet());
    return actionGraph.getArtifactsList().stream()
        .filter(artifact -> artifactIds.contains(artifact.getId()))
        .map(AnalysisProtos.Artifact::getExecPath)
        .filter(path -> suffixes.stream().anyMatch(path::endsWith))
        .collect(Collectors.toList());
  }

  private String getTargetId(String needle) {
    return actionGraph.getTargetsList().stream()
        .filter(target -> needle.equals(target.getLabel()))
        .findFirst()
        .orElse(AnalysisProtos.Target.newBuilder().build())
        .getId();
  }

  private List<AnalysisProtos.Action> getActions(String targetLabel) {
    String targetId = getTargetId(targetLabel);
    return actionGraph.getActionsList().stream()
        .filter(action -> targetId.equals(action.getTargetId()))
        .collect(Collectors.toList());
  }

  private List<AnalysisProtos.Artifact> expandDepsetToArtifacts(Queue<String> idsToExpand) {
    HashSet<String> expandedIds = new HashSet<>();

    HashSet<String> artifactIds = new HashSet<>();
    while (!idsToExpand.isEmpty()) {
      String depsetId = idsToExpand.remove();
      if (expandedIds.contains(depsetId)) {
        continue;
      }
      expandedIds.add(depsetId);
      for (AnalysisProtos.DepSetOfFiles depset : actionGraph.getDepSetOfFilesList()) {
        if (!depsetId.equals(depset.getId())) {
          continue;
        }
        idsToExpand.addAll(depset.getTransitiveDepSetIdsList());
        artifactIds.addAll(depset.getDirectArtifactIdsList());
      }
    }
    return actionGraph.getArtifactsList().stream()
        .filter(artifact -> artifactIds.contains(artifact.getId()))
        .collect(Collectors.toList());
  }
}
