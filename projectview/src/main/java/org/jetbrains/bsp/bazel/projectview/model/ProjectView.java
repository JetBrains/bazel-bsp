package org.jetbrains.bsp.bazel.projectview.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
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

  private ProjectView(
      ProjectViewTargetsSection targets,
      Optional<ProjectViewBazelPathSection> bazelPath,
      Optional<ProjectViewDebuggerAddressSection> debuggerAddress) {
    this.targets = targets;
    this.bazelPath = bazelPath;
    this.debuggerAddress = debuggerAddress;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectView)) return false;
    ProjectView that = (ProjectView) o;
    return targets.equals(that.targets)
        && bazelPath.equals(that.bazelPath)
        && debuggerAddress.equals(that.debuggerAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targets, bazelPath, debuggerAddress);
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
        + '}';
  }

  public static class Builder {

    private List<ProjectView> importedProjectViews = List.of();

    private ProjectViewTargetsSection targets = new ProjectViewTargetsSection();

    private Optional<ProjectViewBazelPathSection> bazelPath = Optional.empty();

    private Optional<ProjectViewDebuggerAddressSection> debuggerAddress = Optional.empty();

    private Builder() {}

    public Builder imports(List<ProjectView> importedProjectViews) {
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

    public ProjectView build() {
      var targets = combineTargetsSection();
      throwIfListSectionIsEmpty(targets);

      var bazelPath = combineBazelPathSection();

      return new ProjectView(targets, bazelPath, debuggerAddress);
    }

    private ProjectViewTargetsSection combineTargetsSection() {
      var includedTargets =
          combineListValuesWithImported(
              targets, ProjectView::getTargets, ProjectViewListSection::getIncludedValues);
      var excludedTargets =
          combineListValuesWithImported(
              targets, ProjectView::getTargets, ProjectViewListSection::getExcludedValues);

      return new ProjectViewTargetsSection(includedTargets, excludedTargets);
    }

    private <T extends ProjectViewListSection> List<String> combineListValuesWithImported(
        T section,
        Function<ProjectView, T> sectionGetter,
        Function<ProjectViewListSection, List<String>> valuesGetter) {
      return importedProjectViews.stream()
          .map(sectionGetter)
          .map(valuesGetter)
          .reduce(valuesGetter.apply(section), ListUtils::concat);
    }

    private Optional<ProjectViewBazelPathSection> combineBazelPathSection() {
      var defaultBazelPathSection = getLastImportedSingletonValue(ProjectView::getBazelPath);

      return bazelPath.or(() -> defaultBazelPathSection);
    }

    private <T extends ProjectViewSingletonSection> Optional<T> getLastImportedSingletonValue(
        Function<ProjectView, Optional<T>> sectionGetter) {
      return importedProjectViews.stream()
          .map(sectionGetter)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .reduce((first, second) -> second);
    }

    private void throwIfListSectionIsEmpty(ProjectViewListSection section) {
      if (isListSectionIsEmpty(section)) {
        throw new IllegalStateException(
            section.getSectionName() + " section cannot have an empty included list!");
      }
    }

    private boolean isListSectionIsEmpty(ProjectViewListSection section) {
      return section.getIncludedValues().isEmpty();
    }
  }
}
