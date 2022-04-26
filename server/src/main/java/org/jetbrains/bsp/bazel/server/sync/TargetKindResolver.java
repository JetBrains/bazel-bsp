package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import java.util.Map;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

public class TargetKindResolver {
    private final Map<String, Tag> ruleSuffixToTargetType =
            Map.of(
                    "library", Tag.LIBRARY,
                    "binary", Tag.APPLICATION,
                    "test", Tag.TEST);

    public Set<Tag> resolveTags(TargetInfo targetInfo) {
        var tag =
                ruleSuffixToTargetType.entrySet().stream()
                        .filter(entry -> targetInfo.getKind().endsWith("_" + entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(Tag.NO_IDE);
        var tagToManual = ruleSuffixToTargetType.entrySet().stream()
                .filter(entry -> targetInfo.getKind().endsWith("_" + entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(Tag.MANUAL);
        if (targetInfo.getTagsList().contains("no-ide")) {
            return HashSet.of(tag, Tag.NO_IDE);
        } else if (targetInfo.getTagsList().contains("manual")) {
            return HashSet.of(tagToManual, Tag.MANUAL);
        }
        return HashSet.of(tag);
    }
}
