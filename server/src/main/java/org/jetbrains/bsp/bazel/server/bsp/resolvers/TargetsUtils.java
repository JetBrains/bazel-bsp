package org.jetbrains.bsp.bazel.server.bsp.resolvers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

public final class TargetsUtils {

  public static List<String> getTargetsUris(List<BuildTargetIdentifier> targets) {
    return targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList());
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
    ProjectViewTargetsSection targetsSection = projectView.getTargets().get();
    String excludedTargets = getExcludedTargets(targetsSection.getExcludedValues());
    String includedTargets = getIncludedTargets(targetsSection.getIncludedValues());

    return String.format("%s %s", includedTargets, excludedTargets);
  }

  public static String getTargetWithExcludedTargets(ProjectView projectView, String target) {
    ProjectViewTargetsSection targetsSection = projectView.getTargets().get();
    String excludedTargets = getExcludedTargets(targetsSection.getExcludedValues());

    return String.format("%s %s", target, excludedTargets);
  }

  private static String getExcludedTargets(List<BuildTargetIdentifier> excludedTargets) {
    return excludedTargets.stream()
        .map(BuildTargetIdentifier::getUri)
        .map(TargetsUtils::addExceptStatement)
        .collect(Collectors.joining(" "));
  }

  private static String getIncludedTargets(List<BuildTargetIdentifier> excludedTargets) {
    return excludedTargets.stream()
        .map(BuildTargetIdentifier::getUri)
        .collect(Collectors.joining(" "));
  }

  private static String addExceptStatement(String target) {
    return String.format("except %s", target);
  }
}
