package org.jetbrains.bsp.bazel.server.bep;

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.BuildEventId;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.NamedSetOfFiles;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.OutputGroup;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class BepOutputBuilder {
  private final Map<String, Set<String>> outputGroups = new HashMap<>();
  private final Map<String, NamedSetOfFiles> fileSets = new HashMap<>();
  private final Set<String> rootTargets = new HashSet<>();

  public void storeNamedSet(String id, NamedSetOfFiles namedSetOfFiles) {
    this.fileSets.put(id, namedSetOfFiles);
  }

  public void storeTargetOutputGroups(String target, List<OutputGroup> outputGroups) {
    rootTargets.add(target);

    for (var group : outputGroups) {
      var fileSets =
          group.getFileSetsList().stream()
              .map(BuildEventId.NamedSetOfFilesId::getId)
              .collect(Collectors.toList());
      this.outputGroups.computeIfAbsent(group.getName(), key -> new HashSet<>()).addAll(fileSets);
    }
  }

  public BepOutput build() {
    return new BepOutput(outputGroups, fileSets, rootTargets);
  }
}
