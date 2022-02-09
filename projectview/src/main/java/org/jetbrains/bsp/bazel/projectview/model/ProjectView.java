package org.jetbrains.bsp.bazel.projectview.model;

import io.vavr.collection.Seq;
import io.vavr.control.Try;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewSingletonSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

/**
 * Representation of the project view file
 *
 * @link https://ij.bazel.build/docs/project-views.html
 */
public class ProjectView {

  private final ProjectViewTargetsSection targets;

  private final Optional<ProjectViewBazelPathSection> bazelPath;

  private final Optional<ProjectViewDebuggerAddressSection> debuggerAddress;

  private final Optional<ProjectViewJavaPathSection> javaPath;

  private ProjectView(
      ProjectViewTargetsSection targets,
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

  public ProjectViewTargetsSection getTargets() {
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

    private ProjectViewTargetsSection targets = new ProjectViewTargetsSection();

    private Optional<ProjectViewBazelPathSection> bazelPath = Optional.empty();

    private Optional<ProjectViewDebuggerAddressSection> debuggerAddress = Optional.empty();

    private Optional<ProjectViewJavaPathSection> javaPath = Optional.empty();

    private Builder() {}

    public Builder imports(List<Try<ProjectView>> importedProjectViews) {
      this.importedProjectViews = importedProjectViews;
      return this;
    }

    public Builder targets(ProjectViewTargetsSection target) {
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
      return Try.sequence(importedProjectViews)
          .map(Seq::toJavaList)
          .flatMap(this::buildWithImports);
    }

    private Try<ProjectView> buildWithImports(List<ProjectView> importedProjectViews) {
      return combineTargetsSection(importedProjectViews)
          .map(targets -> buildWithImportsAndRequiredFields(importedProjectViews, targets));
    }

    private ProjectView buildWithImportsAndRequiredFields(
        List<ProjectView> importedProjectViews, ProjectViewTargetsSection targets) {
      var bazelPath = combineBazelPathSection(importedProjectViews);
      var debuggerAddress = combineDebuggerAddressSection(importedProjectViews);
      var javaPath = combineJavaPathSection(importedProjectViews);

      return new ProjectView(targets, bazelPath, debuggerAddress, javaPath);
    }

    private Try<ProjectViewTargetsSection> combineTargetsSection(
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
      var rawSection = new ProjectViewTargetsSection(includedTargets, excludedTargets);

      return getListSectionOrFailureIfIsEmpty(rawSection);
    }

    private <T extends ProjectViewListSection> List<String> combineListValuesWithImported(
        List<ProjectView> importedProjectViews,
        T section,
        Function<ProjectView, T> sectionGetter,
        Function<ProjectViewListSection, List<String>> valuesGetter) {
      return importedProjectViews.stream()
          .map(sectionGetter)
          .map(valuesGetter)
          .reduce(valuesGetter.apply(section), ListUtils::concat);
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

    private <T extends ProjectViewSingletonSection> Optional<T> getLastImportedSingletonValue(
        List<ProjectView> importedProjectViews, Function<ProjectView, Optional<T>> sectionGetter) {
      return importedProjectViews.stream()
          .map(sectionGetter)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .reduce((first, second) -> second);
    }

    private <T extends ProjectViewListSection> Try<T> getListSectionOrFailureIfIsEmpty(T section) {
      if (isListSectionIsEmpty(section)) {
        return Try.failure(
            new IllegalStateException(
                section.getSectionName() + " section cannot have an empty included list!"));
      }
      return Try.success(section);
    }

    private boolean isListSectionIsEmpty(ProjectViewListSection section) {
      return section.getIncludedValues().isEmpty();
    }
  }
}
