package org.jetbrains.bsp.bazel.server.bsp.resolvers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.stream.Collectors;

public final class TargetsUtils {

  public static List<String> getTargetsUris(List<BuildTargetIdentifier> targets) {
    return targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList());
  }

  public static boolean isAttributeSpecifiedAndHasGivenName(
      Build.Attribute attribute, String name) {
    boolean hasGivenName = attribute.getName().equals(name);

    return hasGivenName && attribute.hasExplicitlySpecified() && attribute.getExplicitlySpecified();
  }
}
