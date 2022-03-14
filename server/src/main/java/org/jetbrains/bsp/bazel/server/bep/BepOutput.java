package org.jetbrains.bsp.bazel.server.bep;

import com.google.common.collect.Queues;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import io.vavr.API;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import java.net.URI;

public class BepOutput {
  private final Map<String, Set<String>> outputGroups;
  private final Map<String, BuildEventStreamProtos.NamedSetOfFiles> fileSets;
  private final Set<String> rootTargets;

  public BepOutput(
      Map<String, Set<String>> outputGroups,
      Map<String, BuildEventStreamProtos.NamedSetOfFiles> fileSets,
      Set<String> rootTargets) {
    this.outputGroups = outputGroups;
    this.fileSets = fileSets;
    this.rootTargets = rootTargets;
  }

  public Set<String> rootTargets() {
    return rootTargets;
  }

  public Set<URI> filesByOutputGroupNameTransitive(String outputGroup) {
    var rootIds = this.outputGroups.getOrElse(outputGroup, HashSet.empty());
    if (rootIds.isEmpty()) {
      return HashSet.empty();
    }

    var result = new java.util.HashSet<URI>();
    var toVisit = Queues.newArrayDeque(rootIds);
    var visited = new java.util.HashSet<String>();

    while (!toVisit.isEmpty()) {
      var fileSetId = toVisit.remove();
      var fileSet = fileSets.apply(fileSetId);

      fileSet.getFilesList().stream()
          .map(API.unchecked(s -> new URI(s.getUri())))
          .forEach(result::add);
      visited.add(fileSetId);

      var children = fileSet.getFileSetsList();
      children.stream()
          .map(BuildEventStreamProtos.BuildEventId.NamedSetOfFilesId::getId)
          .filter(child -> !visited.contains(child))
          .forEach(toVisit::add);
    }

    return HashSet.ofAll(result);
  }
}
