package org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph;

import com.google.common.collect.Lists;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.commons.Uri;

public class ActionGraphV2Parser implements ActionGraphParser {
  private final AnalysisProtosV2.ActionGraphContainer actionGraph;
  private final Map<Integer, AnalysisProtosV2.PathFragment> pathFragmentMap;

  public ActionGraphV2Parser(AnalysisProtosV2.ActionGraphContainer actionGraph) {
    this.actionGraph = actionGraph;
    pathFragmentMap =
        actionGraph.getPathFragmentsList().stream()
            .collect(Collectors.toMap(AnalysisProtosV2.PathFragment::getId, fragment -> fragment));
  }

  @Override
  public List<String> getInputsAsUri(String target, String execRoot) {
    return getInputs(target, Lists.newArrayList(".jar", "js")).stream()
        .map(exec_path -> Uri.fromExecPath(exec_path, execRoot).toString())
        .collect(Collectors.toList());
  }

  private List<String> getInputs(String target, List<String> suffixes) {
    return getActions(target).stream()
            .flatMap(action -> action.getInputDepSetIdsList().stream())
            .flatMap(
                    depset -> {
                      Queue<Integer> queue = new ArrayDeque<>();
                      queue.add(depset);
                      return expandDepsetToArtifacts(queue).stream();
                    })
            .map(AnalysisProtosV2.Artifact::getPathFragmentId)
            .map(pathFragmentId -> "exec-root://" + constructPath(pathFragmentId))
            .filter(path -> suffixes.stream().anyMatch(path::endsWith))
            .collect(Collectors.toCollection(TreeSet::new))
            .stream()
            .collect(Collectors.toList());
  }

  private String constructPath(final Integer pathFragmentId) {
    int currId = pathFragmentId;
    Stack<String> pathStack = new Stack<>();
    while (currId != 0) {
      AnalysisProtosV2.PathFragment fragment = pathFragmentMap.get(currId);
      pathStack.add(fragment.getLabel());
      currId = fragment.getParentId();
    }
    final StringBuilder path = new StringBuilder();
    while (pathStack.size() != 1) path.append(pathStack.pop()).append("/");

    return path.append(pathStack.pop()).toString();
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

  private int getTargetId(String needle) {
    return actionGraph.getTargetsList().stream()
        .filter(target -> needle.equals(target.getLabel()))
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

  private List<AnalysisProtosV2.Artifact> expandDepsetToArtifacts(Queue<Integer> idsToExpand) {
    HashSet<Integer> expandedIds = new HashSet<>();

    HashSet<Integer> artifactIds = new HashSet<>();
    while (!idsToExpand.isEmpty()) {
      Integer depsetId = idsToExpand.remove();
      if (expandedIds.contains(depsetId)) {
        continue;
      }
      expandedIds.add(depsetId);
      for (AnalysisProtosV2.DepSetOfFiles depset : actionGraph.getDepSetOfFilesList()) {
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
