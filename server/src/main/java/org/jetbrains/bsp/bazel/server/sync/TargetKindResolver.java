package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import java.util.Map;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

public class TargetKindResolver {
  private static final Set<Tag> LIBRARY = HashSet.of(Tag.LIBRARY);
  private static final Map<String, Set<Tag>> ruleSuffixToTargetType =
      Map.of(
          "library", LIBRARY,
          "binary", HashSet.of(Tag.APPLICATION),
          "test", HashSet.of(Tag.TEST));

  private static final Set<Tag> NO_IDE = HashSet.of(Tag.NO_IDE);

  public Set<Tag> resolveTags(TargetInfo targetInfo) {
    if (targetInfo.getKind().equals("resources_union")) {
      return LIBRARY;
    }

    var tag =
        ruleSuffixToTargetType.entrySet().stream()
            .filter(entry -> targetInfo.getKind().endsWith("_" + entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(NO_IDE);
    if (targetInfo.getTagsList().contains("no-ide")) {
      return tag.add(Tag.NO_IDE);
    }
    return tag;
  }
}
