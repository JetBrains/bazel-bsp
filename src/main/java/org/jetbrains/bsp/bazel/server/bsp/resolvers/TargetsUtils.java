package org.jetbrains.bsp.bazel.server.bsp.resolvers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.base.Joiner;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.specific.TargetsSection;

public final class TargetsUtils {

  public static List<String> getTargetsUris(List<BuildTargetIdentifier> targets) {
    return targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList());
  }

  public static boolean isAttributeSpecifiedAndHasGivenName(
      Build.Attribute attribute, String name) {
    boolean hasGivenName = attribute.getName().equals(name);

    return hasGivenName && attribute.hasExplicitlySpecified() && attribute.getExplicitlySpecified();
  }

  public static String getTargetsUnion(List<BuildTargetIdentifier> targets) {
    return targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.joining("+"));
  }

  public static String getKindInput(ProjectView projectView, String fileUri, String prefix) {
    return String.format(
        "rdeps(%s, %s, 1)",
        TargetsUtils.getAllProjectTargetsWithExcludedTargets(projectView),
        fileUri.substring(prefix.length()));
  }

  public static String getAllProjectTargetsWithExcludedTargets(ProjectView projectView) {
    TargetsSection targetsSection = projectView.getTargets();
    String excludedTargets = getExcludedTargets(targetsSection.getExcludedTargets());
    String includedTargets = Joiner.on(" ").join(targetsSection.getIncludedTargets());

    return String.format("%s %s", includedTargets, excludedTargets);
  }

  public static String getTargetWithExcludedTargets(ProjectView projectView, String target) {
    TargetsSection targetsSection = projectView.getTargets();
    String excludedTargets = getExcludedTargets(targetsSection.getExcludedTargets());

    return String.format("%s %s", target, excludedTargets);
  }

  private static String getExcludedTargets(List<String> excludedTargets) {
    return excludedTargets.stream()
        .map(TargetsUtils::addExceptStatement)
        .collect(Collectors.joining(" "));
  }

  private static String addExceptStatement(String target) {
    return String.format("except %s", target);
  }
}
