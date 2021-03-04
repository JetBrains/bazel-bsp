package org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph;

import com.google.common.collect.Lists;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionGraphV2Parser extends ActionGraphParser {
  private final AnalysisProtosV2.ActionGraphContainer actionGraph;
  private final Map<Integer, AnalysisProtosV2.PathFragment> pathFragmentMap;

  public ActionGraphV2Parser(AnalysisProtosV2.ActionGraphContainer actionGraph) {
    this.actionGraph = actionGraph;
    pathFragmentMap =
        actionGraph.getPathFragmentsList().stream()
            .collect(Collectors.toMap(AnalysisProtosV2.PathFragment::getId, fragment -> fragment));
  }

  @Override
  protected Stream<String> buildInputs(String target, List<String> suffixes) {
    return getActions(target).stream()
        .flatMap(action -> action.getInputDepSetIdsList().stream())
        .flatMap(depset -> expandDepsetToArtifacts(depset).stream())
        .map(AnalysisProtosV2.Artifact::getPathFragmentId)
        .map(pathFragmentId -> EXEC_ROOT + constructPath(pathFragmentId));
  }

  private String constructPath(Integer pathFragmentId) {
    int currId = pathFragmentId;
    List<String> pathBuilder = new ArrayList<>();
    while (currId != 0) {
      AnalysisProtosV2.PathFragment fragment = pathFragmentMap.get(currId);
      pathBuilder.add(fragment.getLabel());
      currId = fragment.getParentId();
    }

    Collections.reverse(pathBuilder);
    return String.join("/", pathBuilder);
  }

  @Override
  public List<String> getOutputs(String target, List<String> suffixes) {
    Set<Integer> artifactIds =
        getActions(target).stream()
            .flatMap(action -> action.getOutputIdsList().stream())
            .collect(Collectors.toSet());
    return actionGraph.getArtifactsList().stream()
        .filter(artifact -> artifactIds.contains(artifact.getId()))
        .map(AnalysisProtosV2.Artifact::getPathFragmentId)
        .map(this::constructPath)
        .filter(path -> suffixes.stream().anyMatch(path::endsWith))
        .collect(Collectors.toList());
  }

  private int getTargetId(String targetLabel) {
    return actionGraph.getTargetsList().stream()
        .filter(target -> targetLabel.equals(target.getLabel()))
        .findFirst()
        .orElse(AnalysisProtosV2.Target.newBuilder().build())
        .getId();
  }

  private List<AnalysisProtosV2.Action> getActions(String targetLabel) {
    int targetId = getTargetId(targetLabel);
    return actionGraph.getActionsList().stream()
        .filter(action -> targetId == action.getTargetId())
        .collect(Collectors.toList());
  }

  private List<AnalysisProtosV2.Artifact> expandDepsetToArtifacts(Integer idToExpand) {
    Queue<Integer> idsToExpand = new ArrayDeque<>(Lists.newArrayList(idToExpand));

    HashSet<Integer> expandedIds = new HashSet<>();

    HashSet<Integer> artifactIds = new HashSet<>();
    while (!idsToExpand.isEmpty()) {
      Integer depsetId = idsToExpand.remove();
      if (expandedIds.contains(depsetId)) {
        return Collections.emptyList();
      }
      expandedIds.add(depsetId);

      actionGraph.getDepSetOfFilesList().stream()
          .filter((depset) -> depsetId.equals(depset.getId()))
          .forEach(
              (depset) -> {
                idsToExpand.addAll(depset.getTransitiveDepSetIdsList());
                artifactIds.addAll(depset.getDirectArtifactIdsList());
              });
    }
    return actionGraph.getArtifactsList().stream()
        .filter(artifact -> artifactIds.contains(artifact.getId()))
        .collect(Collectors.toList());
  }
}
