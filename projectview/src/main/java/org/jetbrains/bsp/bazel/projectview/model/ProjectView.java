package org.jetbrains.bsp.bazel.projectview.model;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.commons.ListUtils;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewListSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Representation of the project view file
 *
 * @link https://ij.bazel.build/docs/project-views.html
 */
public class ProjectView {

  private final ProjectViewTargetsSection targets;

  private ProjectView(ProjectViewTargetsSection targets) {
    this.targets = targets;
  }

  public static ProjectView.Builder builder() {
    return new Builder();
  }

  public ProjectViewTargetsSection getTargets() {
    return targets;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProjectView)) return false;
    ProjectView that = (ProjectView) o;
    return targets.equals(that.targets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targets);
  }

  @Override
  public String toString() {
    return "ProjectView{" + "targets=" + targets.toString() + '}';
  }

  public static class Builder {

    private List<ProjectView> importedProjectViews = ImmutableList.of();

    private ProjectViewTargetsSection targets = new ProjectViewTargetsSection();

    private Builder() {}

    public Builder imports(List<ProjectView> importedProjectViews) {
      this.importedProjectViews = importedProjectViews;
      return this;
    }

    public Builder targets(ProjectViewTargetsSection target) {
      this.targets = target;
      return this;
    }

    public ProjectView build() {
      ProjectViewTargetsSection targets = combineTargetsSection();
      throwIfListSectionIsEmpty(targets);

      return new ProjectView(targets);
    }

    private ProjectViewTargetsSection combineTargetsSection() {
      List<String> includedTargets =
          combineListValuesWithImported(
              targets, ProjectView::getTargets, ProjectViewListSection::getIncludedValues);
      List<String> excludedTargets =
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

    private void throwIfListSectionIsEmpty(ProjectViewListSection listSection) {
      if (listSection.getIncludedValues().isEmpty()) {
        throw new IllegalStateException(
            String.format(
                "`%s` section cannot have an empty included list!", listSection.getSectionName()));
      }
    }
  }
}
