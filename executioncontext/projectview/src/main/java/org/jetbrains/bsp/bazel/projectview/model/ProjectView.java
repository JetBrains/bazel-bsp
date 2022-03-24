package org.jetbrains.bsp.bazel.projectview.model;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewExcludableListSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

/**
 * Representation of the project view file.
 *
 * @link https://ij.bazel.build/docs/project-views.html
 */
public class ProjectView {

  private static final Logger log = LogManager.getLogger(ProjectView.class);

  /** targets included and excluded from the project */
  private final Option<ProjectViewTargetsSection> targets;

  /** bazel path used to invoke bazel from the code */
  private final Option<ProjectViewBazelPathSection> bazelPath;

  /** debugger address which can be added to the server run command (as a flag to java) */
  private final Option<ProjectViewDebuggerAddressSection> debuggerAddress;

  /** path to java to run a server */
  private final Option<ProjectViewJavaPathSection> javaPath;

  /** bazel flags added to all bazel command invocations */
  private final Option<ProjectViewBuildFlagsSection> buildFlags;

  private ProjectView(
      Option<ProjectViewTargetsSection> targets,
      Option<ProjectViewBazelPathSection> bazelPath,
      Option<ProjectViewDebuggerAddressSection> debuggerAddress,
      Option<ProjectViewJavaPathSection> javaPath,
      Option<ProjectViewBuildFlagsSection> buildFlags) {
    this.targets = targets;
    this.bazelPath = bazelPath;
    this.debuggerAddress = debuggerAddress;
    this.javaPath = javaPath;
    this.buildFlags = buildFlags;
  }

  public static ProjectView.Builder builder() {
    return new Builder();
  }

  public Option<ProjectViewTargetsSection> getTargets() {
    return targets;
  }

  public TargetSpecs targetSpecs() {
    return targets
        .map(s -> new TargetSpecs(s.getIncludedValues(), s.getExcludedValues()))
        .getOrElse(TargetSpecs.empty());
  }

  public Option<ProjectViewBazelPathSection> getBazelPath() {
    return bazelPath;
  }

  public Option<ProjectViewDebuggerAddressSection> getDebuggerAddress() {
    return debuggerAddress;
  }

  public Option<ProjectViewJavaPathSection> getJavaPath() {
    return javaPath;
  }

  public Option<ProjectViewBuildFlagsSection> getBuildFlags() {
    return buildFlags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectView)) return false;
    ProjectView that = (ProjectView) o;
    return targets.equals(that.targets)
        && bazelPath.equals(that.bazelPath)
        && debuggerAddress.equals(that.debuggerAddress)
        && javaPath.equals(that.javaPath)
        && buildFlags.equals(that.buildFlags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targets, bazelPath, debuggerAddress, javaPath, buildFlags);
  }

  @Override
  public String toString() {
    return "ProjectView{"
        + "targets="
        + targets
        + ", bazelPath="
        + bazelPath
        + ", debuggerAddress="
        + debuggerAddress
        + ", javaPath="
        + javaPath
        + ", buildFlags="
        + buildFlags
        + '}';
  }

  public static class Builder {

    private List<Try<ProjectView>> importedProjectViews = List.of();

    private Option<ProjectViewTargetsSection> targets = Option.none();

    private Option<ProjectViewBazelPathSection> bazelPath = Option.none();

    private Option<ProjectViewDebuggerAddressSection> debuggerAddress = Option.none();

    private Option<ProjectViewJavaPathSection> javaPath = Option.none();

    private Option<ProjectViewBuildFlagsSection> buildFlags = Option.none();

    private Builder() {}

    public Builder imports(List<Try<ProjectView>> importedProjectViews) {
      this.importedProjectViews = importedProjectViews;
      return this;
    }

    public Builder targets(Option<ProjectViewTargetsSection> target) {
      this.targets = target;
      return this;
    }

    public Builder bazelPath(Option<ProjectViewBazelPathSection> bazelPath) {
      this.bazelPath = bazelPath;
      return this;
    }

    public Builder debuggerAddress(Option<ProjectViewDebuggerAddressSection> debuggerAddress) {
      this.debuggerAddress = debuggerAddress;
      return this;
    }

    public Builder javaPath(Option<ProjectViewJavaPathSection> javaPath) {
      this.javaPath = javaPath;
      return this;
    }

    public Builder buildFlags(Option<ProjectViewBuildFlagsSection> buildFlags) {
      this.buildFlags = buildFlags;
      return this;
    }

    public Try<ProjectView> build() {
      log.debug(
          "Building project view with"
              + " imported project views: {}"
              + " and (before combining with imported project views)"
              + " targets: {},"
              + " bazel path: {},"
              + " debugger address: {},"
              + " java path: {},"
              + " build flags: {}.",
          importedProjectViews,
          targets,
          bazelPath,
          debuggerAddress,
          javaPath,
          buildFlags);

      return Try.sequence(importedProjectViews).map(Seq::toList).map(this::buildWithImports);
    }

    private ProjectView buildWithImports(List<ProjectView> importedProjectViews) {
      var targets = combineTargetsSection(importedProjectViews);
      var bazelPath = combineBazelPathSection(importedProjectViews);
      var debuggerAddress = combineDebuggerAddressSection(importedProjectViews);
      var javaPath = combineJavaPathSection(importedProjectViews);
      var buildFlags = combineBuildFlagsSection(importedProjectViews);

      log.debug(
          "Building project view with combined"
              + " targets: {},"
              + " bazel path: {},"
              + " debugger address: {},"
              + " java path: {}.",
          targets,
          bazelPath,
          debuggerAddress,
          javaPath);

      return new ProjectView(targets, bazelPath, debuggerAddress, javaPath, buildFlags);
    }

    private Option<ProjectViewTargetsSection> combineTargetsSection(
        List<ProjectView> importedProjectViews) {
      var includedTargets =
          combineListValuesWithImported(
              importedProjectViews,
              targets,
              ProjectView::getTargets,
              ProjectViewExcludableListSection::getValues);
      var excludedTargets =
          combineListValuesWithImported(
              importedProjectViews,
              targets,
              ProjectView::getTargets,
              ProjectViewExcludableListSection::getExcludedValues);

      return createInstanceOfExcludableListSectionOrEmpty(
          includedTargets, excludedTargets, ProjectViewTargetsSection::new);
    }

    private Option<ProjectViewBuildFlagsSection> combineBuildFlagsSection(
        List<ProjectView> importedProjectViews) {
      var flags =
          combineListValuesWithImported(
              importedProjectViews,
              buildFlags,
              ProjectView::getBuildFlags,
              ProjectViewListSection::getValues);

      return createInstanceOfListSectionOrEmpty(flags, ProjectViewBuildFlagsSection::new);
    }

    private <V, S extends ProjectViewListSection<V>, T extends Option<S>>
        List<V> combineListValuesWithImported(
            List<ProjectView> importedProjectViews,
            T section,
            Function<ProjectView, T> sectionGetter,
            Function<S, List<V>> valuesGetter) {
      var sectionValues = section.map(valuesGetter).getOrElse(List.of());

      return importedProjectViews
          .map(sectionGetter)
          .flatMap(Option::toList)
          .map(valuesGetter)
          .foldLeft(sectionValues, List::appendAll);
    }

    private <V, T extends ProjectViewExcludableListSection<V>>
        Option<T> createInstanceOfExcludableListSectionOrEmpty(
            List<V> includedElements,
            List<V> excludedElements,
            BiFunction<List<V>, List<V>, T> constructor) {
      var areListsEmpty = includedElements.isEmpty() && excludedElements.isEmpty();
      var isAnyElementInLists = !areListsEmpty;

      return Option.when(
          isAnyElementInLists, constructor.apply(includedElements, excludedElements));
    }

    private <V, T extends ProjectViewListSection<V>> Option<T> createInstanceOfListSectionOrEmpty(
        List<V> values, Function<List<V>, T> constructor) {
      var isAnyElementInList = !values.isEmpty();

      return Option.when(isAnyElementInList, constructor.apply(values));
    }

    private Option<ProjectViewBazelPathSection> combineBazelPathSection(
        List<ProjectView> importedProjectViews) {
      var defaultBazelPathSection =
          getLastImportedSingletonValue(importedProjectViews, ProjectView::getBazelPath);

      return bazelPath.orElse(defaultBazelPathSection);
    }

    private Option<ProjectViewDebuggerAddressSection> combineDebuggerAddressSection(
        List<ProjectView> importedProjectViews) {
      var defaultDebuggerAddressSection =
          getLastImportedSingletonValue(importedProjectViews, ProjectView::getDebuggerAddress);

      return debuggerAddress.orElse(defaultDebuggerAddressSection);
    }

    private Option<ProjectViewJavaPathSection> combineJavaPathSection(
        List<ProjectView> importedProjectViews) {
      var defaultJavaPathSection =
          getLastImportedSingletonValue(importedProjectViews, ProjectView::getJavaPath);

      return javaPath.orElse(defaultJavaPathSection);
    }

    private <V, T extends ProjectViewSingletonSection<V>> Option<T> getLastImportedSingletonValue(
        List<ProjectView> importedProjectViews, Function<ProjectView, Option<T>> sectionGetter) {
      return importedProjectViews.map(sectionGetter).findLast(Option::isDefined).flatMap(x -> x);
    }
  }
}
