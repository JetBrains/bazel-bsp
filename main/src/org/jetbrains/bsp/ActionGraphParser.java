package org.jetbrains.bsp;

import com.google.devtools.build.lib.analysis.AnalysisProtos;

import java.util.*;
import java.util.stream.Collectors;

// TODO: Index, cache, etc
public class ActionGraphParser {
  private final AnalysisProtos.ActionGraphContainer actionGraph;

  public ActionGraphParser(AnalysisProtos.ActionGraphContainer actionGraph) {
    this.actionGraph = actionGraph;
  }

  public List<String> getInputs(String target, String suffix) {
    return getActions(target).stream()
        .flatMap(action -> action.getInputDepSetIdsList().stream())
        .flatMap(
            depset -> {
              Queue<String> queue = new ArrayDeque<>();
              queue.add(depset);
              return expandDepsetToArtifacts(queue).stream();
            })
        .map(artifact -> "exec-root://" + artifact.getExecPath())
        .filter(path -> path.endsWith(suffix))
        .collect(Collectors.toCollection(TreeSet::new))
        .stream()
        .collect(Collectors.toList());
  }

  public List<String> getInputsAsUri(String target, String suffix, String execRoot) {
    return getInputs(target, ".jar").stream()
            .map(exec_path -> Uri.fromExecPath(exec_path, execRoot).toString())
            .collect(Collectors.toList());
  }

  public List<String> getOutputs(String target, String suffix) {
    Set<String> artifactIds =
        getActions(target).stream()
            .flatMap(action -> action.getOutputIdsList().stream())
            .collect(Collectors.toSet());
    return actionGraph.getArtifactsList().stream()
        .filter(artifact -> artifactIds.contains(artifact.getId()))
        .map(artifact -> artifact.getExecPath())
        .filter(path -> path.endsWith(suffix))
        .collect(Collectors.toList());
  }

  private String getTargetId(String needle) {
    return actionGraph.getTargetsList().stream()
        .filter(target -> needle.equals(target.getLabel()))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Could not find " + needle + ". Targets found: " + Arrays.toString(actionGraph.getTargetsList().toArray())))
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
