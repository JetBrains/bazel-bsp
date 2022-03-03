package org.jetbrains.bsp.bazel.projectview.model;

import io.vavr.collection.Seq;
import io.vavr.control.Try;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
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
  private final Optional<ProjectViewTargetsSection> targets;

  /** bazel path used to invoke bazel from the code */
  private final Optional<ProjectViewBazelPathSection> bazelPath;

  /** debugger address which can be added to the server run command (as a flag to java) */
  private final Optional<ProjectViewDebuggerAddressSection> debuggerAddress;

  /** path to java to run a server */
  private final Optional<ProjectViewJavaPathSection> javaPath;

  private ProjectView(
      Optional<ProjectViewTargetsSection> targets,
      Optional<ProjectViewBazelPathSection> bazelPath,
      Optional<ProjectViewDebuggerAddressSection> debuggerAddress,
      Optional<ProjectViewJavaPathSection> javaPath) {
    this.targets = targets;
    this.bazelPath = bazelPath;
    this.debuggerAddress = debuggerAddress;
    this.javaPath = javaPath;
  }

  public static ProjectView.Builder builder() {
    return new Builder();
  }

  public Optional<ProjectViewTargetsSection> getTargets() {
    return targets;
  }

  public Optional<ProjectViewBazelPathSection> getBazelPath() {
    return bazelPath;
  }

  public Optional<ProjectViewDebuggerAddressSection> getDebuggerAddress() {
    return debuggerAddress;
  }

  public Optional<ProjectViewJavaPathSection> getJavaPath() {
    return javaPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectView)) return false;
    ProjectView that = (ProjectView) o;
    return targets.equals(that.targets)
        && bazelPath.equals(that.bazelPath)
        && debuggerAddress.equals(that.debuggerAddress)
        && javaPath.equals(that.javaPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targets, bazelPath, debuggerAddress, javaPath);
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
        + '}';
  }

  public static class Builder {

    private List<Try<ProjectView>> importedProjectViews = List.of();

    private Optional<ProjectViewTargetsSection> targets = Optional.empty();

    private Optional<ProjectViewBazelPathSection> bazelPath = Optional.empty();

    private Optional<ProjectViewDebuggerAddressSection> debuggerAddress = Optional.empty();

    private Optional<ProjectViewJavaPathSection> javaPath = Optional.empty();

    private Builder() {}

    public Builder imports(List<Try<ProjectView>> importedProjectViews) {
      this.importedProjectViews = importedProjectViews;
      return this;
    }

    public Builder targets(Optional<ProjectViewTargetsSection> target) {
      this.targets = target;
      return this;
    }

    public Builder bazelPath(Optional<ProjectViewBazelPathSection> bazelPath) {
      this.bazelPath = bazelPath;
      return this;
    }

    public Builder debuggerAddress(Optional<ProjectViewDebuggerAddressSection> debuggerAddress) {
      this.debuggerAddress = debuggerAddress;
      return this;
    }

    public Builder javaPath(Optional<ProjectViewJavaPathSection> javaPath) {
      this.javaPath = javaPath;
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
              + " java path: {}.",
          importedProjectViews,
          targets,
          bazelPath,
          debuggerAddress,
          javaPath);

      return Try.sequence(importedProjectViews).map(Seq::toJavaList).map(this::buildWithImports);
    }

    private ProjectView buildWithImports(List<ProjectView> importedProjectViews) {
      var targets = combineTargetsSection(importedProjectViews);
      var bazelPath = combineBazelPathSection(importedProjectViews);
      var debuggerAddress = combineDebuggerAddressSection(importedProjectViews);
      var javaPath = combineJavaPathSection(importedProjectViews);

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

      return new ProjectView(targets, bazelPath, debuggerAddress, javaPath);
    }

    private Optional<ProjectViewTargetsSection> combineTargetsSection(
        List<ProjectView> importedProjectViews) {
      var includedTargets =
          combineListValuesWithImported(
              importedProjectViews,
              targets,
              ProjectView::getTargets,
              ProjectViewListSection::getIncludedValues);
      var excludedTargets =
          combineListValuesWithImported(
              importedProjectViews,
              targets,
              ProjectView::getTargets,
              ProjectViewListSection::getExcludedValues);

      return createInstanceOfListSectionOrEmpty(
          includedTargets, excludedTargets, ProjectViewTargetsSection::new);
    }

    private <V, S extends ProjectViewListSection<V>, T extends Optional<S>>
        List<V> combineListValuesWithImported(
            List<ProjectView> importedProjectViews,
            T section,
            Function<ProjectView, T> sectionGetter,
            Function<S, List<V>> valuesGetter) {
      var sectionValues = section.map(valuesGetter).orElse(List.of());

      return importedProjectViews.stream()
          .map(sectionGetter)
          .flatMap(Optional::stream)
          .map(valuesGetter)
          .reduce(sectionValues, ListUtils::concat);
    }

    private <V, T extends ProjectViewListSection<V>> Optional<T> createInstanceOfListSectionOrEmpty(
        List<V> includedElements,
        List<V> excludedElements,
        BiFunction<List<V>, List<V>, T> constructor) {
      if (includedElements.isEmpty() && excludedElements.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(constructor.apply(includedElements, excludedElements));
    }

    private Optional<ProjectViewBazelPathSection> combineBazelPathSection(
        List<ProjectView> importedProjectViews) {
      var defaultBazelPathSection =
          getLastImportedSingletonValue(importedProjectViews, ProjectView::getBazelPath);

      return bazelPath.or(() -> defaultBazelPathSection);
    }

    private Optional<ProjectViewDebuggerAddressSection> combineDebuggerAddressSection(
        List<ProjectView> importedProjectViews) {
      var defaultDebuggerAddressSection =
          getLastImportedSingletonValue(importedProjectViews, ProjectView::getDebuggerAddress);

      return debuggerAddress.or(() -> defaultDebuggerAddressSection);
    }

    private Optional<ProjectViewJavaPathSection> combineJavaPathSection(
        List<ProjectView> importedProjectViews) {
      var defaultJavaPathSection =
          getLastImportedSingletonValue(importedProjectViews, ProjectView::getJavaPath);

      return javaPath.or(() -> defaultJavaPathSection);
    }

    private <V, T extends ProjectViewSingletonSection<V>> Optional<T> getLastImportedSingletonValue(
        List<ProjectView> importedProjectViews, Function<ProjectView, Optional<T>> sectionGetter) {
      return importedProjectViews.stream()
          .map(sectionGetter)
          .flatMap(Optional::stream)
          .reduce((first, second) -> second);
    }
  }
}
