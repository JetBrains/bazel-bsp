package org.jetbrains.bsp.bazel.server.bsp.resolvers.targets;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import java.util.List;
import java.util.stream.Collectors;

public final class TargetsUtils {

  public static List<String> getTargetsUris(List<BuildTargetIdentifier> targets) {
    return targets.stream()
        .map(BuildTargetIdentifier::getUri)
        .collect(Collectors.toList());
  }
}
