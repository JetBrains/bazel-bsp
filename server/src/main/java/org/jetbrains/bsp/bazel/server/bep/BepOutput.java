package org.jetbrains.bsp.bazel.server.bep;

import com.google.common.collect.Queues;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BepOutput {
  private final Map<String, Set<String>> outputGroups;
  private final Map<String, TextProtoDepSet> textProtoFileSets;
  private final Set<String> rootTargets;

  public BepOutput(
      Map<String, Set<String>> outputGroups,
      Map<String, TextProtoDepSet> textProtoFileSets,
      Set<String> rootTargets) {
    this.outputGroups = outputGroups;
    this.textProtoFileSets = textProtoFileSets;
    this.rootTargets = rootTargets;
  }

  public Set<String> rootTargets() {
    return rootTargets;
  }

  public Set<URI> filesByOutputGroupNameTransitive(String outputGroup) {
    var rootIds = this.outputGroups.getOrDefault(outputGroup, Collections.emptySet());
    if (rootIds.isEmpty()) {
      return Collections.emptySet();
    }

    var result = new HashSet<URI>(rootIds.size());
    var toVisit = Queues.newArrayDeque(rootIds);
    var visited = new HashSet<String>(rootIds.size());

    while (!toVisit.isEmpty()) {
      var fileSetId = toVisit.remove();
      var fileSet = textProtoFileSets.get(fileSetId);

      result.addAll(fileSet.getFiles());
      visited.add(fileSetId);

      var children = fileSet.getChildren();
      children.stream().filter(child -> !visited.contains(child)).forEach(toVisit::add);
    }

    return result;
  }
}
