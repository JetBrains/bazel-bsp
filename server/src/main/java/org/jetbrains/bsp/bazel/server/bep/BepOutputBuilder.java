package org.jetbrains.bsp.bazel.server.bep;

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.BuildEventId;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.NamedSetOfFiles;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.OutputGroup;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class BepOutputBuilder {
  private final Map<String, Set<String>> outputGroups = new HashMap<>();
  private final Map<String, TextProtoDepSet> textProtoFileSets = new HashMap<>();
  private final Set<String> rootTargets = new HashSet<>();

  public void storeNamedSet(String id, NamedSetOfFiles namedSetOfFiles) {
    this.textProtoFileSets.put(
        id,
        new TextProtoDepSet(
            namedSetOfFiles.getFilesList().stream()
                .filter(it -> it.getName().endsWith("bsp-info.textproto"))
                .map(it -> URI.create(it.getUri()))
                .collect(Collectors.toList()),
            namedSetOfFiles.getFileSetsList().stream()
                .map(it -> it.getId())
                .collect(Collectors.toList())));
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
    return new BepOutput(outputGroups, textProtoFileSets, rootTargets);
  }

  private final Logger LOGGER = LogManager.getLogger(BepOutputBuilder.class);
}
