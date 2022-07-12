package org.jetbrains.bsp.bazel.server.bep;

import com.google.common.collect.Queues;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    var rootIds = this.outputGroups.getOrDefault(outputGroup, Collections.emptySet());
    if (rootIds.isEmpty()) {
      return Collections.emptySet();
    }

    var result = new HashSet<URI>();
    var toVisit = Queues.newArrayDeque(rootIds);
    var visited = new HashSet<String>();

    while (!toVisit.isEmpty()) {
      var fileSetId = toVisit.remove();
      var fileSet = fileSets.get(fileSetId);

      fileSet.getFilesList().stream()
          .map(s -> uncheckedURICreation(s.getUri()))
          .forEach(result::add);
      visited.add(fileSetId);

      var children = fileSet.getFileSetsList();
      children.stream()
          .map(BuildEventStreamProtos.BuildEventId.NamedSetOfFilesId::getId)
          .filter(child -> !visited.contains(child))
          .forEach(toVisit::add);
    }

    return result;
  }

  private URI uncheckedURICreation(String s) {
    try {
      return new URI(s);
    } catch (URISyntaxException ignored) {
    }
    return null;
  }
}
