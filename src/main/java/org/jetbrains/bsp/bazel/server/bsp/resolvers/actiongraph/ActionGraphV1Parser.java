package org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph;

import com.google.devtools.build.lib.analysis.AnalysisProtos;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO(illicitonion): Index, cache, etc
public class ActionGraphV1Parser extends ActionGraphParser {
  private final AnalysisProtos.ActionGraphContainer actionGraph;

  public ActionGraphV1Parser(AnalysisProtos.ActionGraphContainer actionGraph) {
    this.actionGraph = actionGraph;
  }

  @Override
  protected Stream<String> buildInputs(String target, List<String> suffixes) {
    return getActions(target).stream()
        .flatMap(action -> action.getInputDepSetIdsList().stream())
        .flatMap(
            depset -> expandDepsetToArtifacts(depset).stream())
        .map(artifact -> EXEC_ROOT + artifact.getExecPath());
  }

  @Override
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

  private String getTargetId(String targetLabel) {
    return actionGraph.getTargetsList().stream()
        .filter(target -> targetLabel.equals(target.getLabel()))
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

  private List<AnalysisProtos.Artifact> expandDepsetToArtifacts(String idToExpand) {
    Queue<String> idsToExpand = new ArrayDeque<>(){{
      add(idToExpand);
    }};

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
