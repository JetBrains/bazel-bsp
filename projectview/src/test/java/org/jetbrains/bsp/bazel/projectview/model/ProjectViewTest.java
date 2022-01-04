package org.jetbrains.bsp.bazel.projectview.model;

import com.google.common.collect.ImmutableList;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProjectViewTest {

  // targets

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForBuilderWithoutTargets() {
    // given & when
    ProjectView.builder().build();

    // then
    // throw an exception
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForBuilderWithoutIncludedTargets() {
    // given & when
    ProjectView.builder()
        .targets(
            new ProjectViewTargetsSection(
                ImmutableList.of(), ImmutableList.of("//excluded_target")))
        .build();

    // then
    // throw an exception
  }

  @Test
  public void shouldNotThrowExceptionForBuilderWithoutTargetsButWithImportedTargets() {
    // given
    ProjectView importedProjectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    // when
    ProjectView projectView =
        ProjectView.builder().imports(ImmutableList.of(importedProjectView)).build();

    // then
    ProjectViewTargetsSection expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(
                "//included_target1.1", "//included_target1.2", "//included_target1.3"),
            ImmutableList.of("//excluded_target1.1", "//excluded_target1.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnEqualTargetsForBuilderWithoutImports() {
    // given & when
    ProjectView projectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1", "//included_target2", "//included_target3"),
                    ImmutableList.of("//excluded_target1", "//excluded_target2")))
            .build();

    // then
    ProjectViewTargetsSection expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//included_target1", "//included_target2", "//included_target3"),
            ImmutableList.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnEqualTargetsForBuilderWithEmptyImports() {
    // given & when
    ProjectView projectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1", "//included_target2", "//included_target3"),
                    ImmutableList.of("//excluded_target1", "//excluded_target2")))
            .build();

    // then
    ProjectViewTargetsSection expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//included_target1", "//included_target2", "//included_target3"),
            ImmutableList.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnCombinedTargetsForOneImportedProjectView() {
    // given
    ProjectView importedProjectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    // when
    ProjectView projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView))
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target2.1", "//included_target2.2", "//included_target2.3"),
                    ImmutableList.of("//excluded_target2.1", "//excluded_target2.2")))
            .build();

    // then
    ProjectViewTargetsSection expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(
                "//included_target1.1",
                "//included_target1.2",
                "//included_target1.3",
                "//included_target2.1",
                "//included_target2.2",
                "//included_target2.3"),
            ImmutableList.of(
                "//excluded_target1.1",
                "//excluded_target1.2",
                "//excluded_target2.1",
                "//excluded_target2.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnCombinedTargetsForThreeImportedProjectView() {
    // given
    ProjectView importedProjectView1 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    ProjectView importedProjectView2 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target2.1"),
                    ImmutableList.of("//excluded_target2.1")))
            .build();

    ProjectView importedProjectView3 =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView1, importedProjectView2))
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target3.1", "//included_target3.2"),
                    ImmutableList.of()))
            .build();

    // when
    ProjectView projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView3))
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    ImmutableList.of("//excluded_target4.1", "//excluded_target4.2")))
            .build();

    // then
    ProjectViewTargetsSection expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(
                "//included_target1.1",
                "//included_target1.2",
                "//included_target1.3",
                "//included_target2.1",
                "//included_target3.1",
                "//included_target3.2",
                "//included_target4.1",
                "//included_target4.2",
                "//included_target4.3"),
            ImmutableList.of(
                "//excluded_target1.1",
                "//excluded_target1.2",
                "//excluded_target2.1",
                "//excluded_target4.1",
                "//excluded_target4.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnCombinedTargetsForNeastedImportedProjectView() {
    // given
    ProjectView importedProjectView1 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    ProjectView importedProjectView2 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target2.1"),
                    ImmutableList.of("//excluded_target2.1")))
            .build();

    ProjectView importedProjectView3 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target3.1", "//included_target3.2"),
                    ImmutableList.of()))
            .build();

    // when
    ProjectView projectView =
        ProjectView.builder()
            .imports(
                ImmutableList.of(importedProjectView1, importedProjectView2, importedProjectView3))
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    ImmutableList.of("//excluded_target4.1", "//excluded_target4.2")))
            .build();

    // then
    ProjectViewTargetsSection expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(
                "//included_target1.1",
                "//included_target1.2",
                "//included_target1.3",
                "//included_target2.1",
                "//included_target3.1",
                "//included_target3.2",
                "//included_target4.1",
                "//included_target4.2",
                "//included_target4.3"),
            ImmutableList.of(
                "//excluded_target1.1",
                "//excluded_target1.2",
                "//excluded_target2.1",
                "//excluded_target4.1",
                "//excluded_target4.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }
}
