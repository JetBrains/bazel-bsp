package org.jetbrains.bsp.bazel.projectview.model;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class ProjectViewTest {

  private static final ProjectViewTargetsSection dummyTargetsSection =
      new ProjectViewTargetsSection(
          ImmutableList.of("//dummy_included_target"), ImmutableList.of());

  private final Optional<ProjectViewBazelPathSection> dummyBazelPathSection = Optional.empty();

  // targets

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForBuilderWithoutTargets() {
    // given & when
    ProjectView.builder().bazelPath(dummyBazelPathSection).build();

    // then
    // throw an exception
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForBuilderWithoutIncludedTargets() {
    // given & when
    ProjectView.builder()
        .bazelPath(dummyBazelPathSection)
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
    var importedProjectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView))
            .bazelPath(dummyBazelPathSection)
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of(
                "//included_target1.1", "//included_target1.2", "//included_target1.3"),
            ImmutableList.of("//excluded_target1.1", "//excluded_target1.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnEqualTargetsForBuilderWithoutImports() {
    // given & when
    var projectView =
        ProjectView.builder()
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1", "//included_target2", "//included_target3"),
                    ImmutableList.of("//excluded_target1", "//excluded_target2")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//included_target1", "//included_target2", "//included_target3"),
            ImmutableList.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnEqualTargetsForBuilderWithEmptyImports() {
    // given & when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1", "//included_target2", "//included_target3"),
                    ImmutableList.of("//excluded_target1", "//excluded_target2")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            ImmutableList.of("//included_target1", "//included_target2", "//included_target3"),
            ImmutableList.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());
  }

  @Test
  public void shouldReturnCombinedTargetsForOneImportedProjectView() {
    // given
    var importedProjectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView))
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target2.1", "//included_target2.2", "//included_target2.3"),
                    ImmutableList.of("//excluded_target2.1", "//excluded_target2.2")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
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
    var importedProjectView1 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    var importedProjectView2 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target2.1"),
                    ImmutableList.of("//excluded_target2.1")))
            .build();

    var importedProjectView3 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target3.1", "//included_target3.2"),
                    ImmutableList.of()))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(
                ImmutableList.of(importedProjectView1, importedProjectView2, importedProjectView3))
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    ImmutableList.of("//excluded_target4.1", "//excluded_target4.2")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
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
  public void shouldReturnCombinedTargetsForNestedImportedProjectView() {
    // given
    var importedProjectView1 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    ImmutableList.of("//excluded_target1.1", "//excluded_target1.2")))
            .build();

    var importedProjectView2 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target2.1"),
                    ImmutableList.of("//excluded_target2.1")))
            .build();

    var importedProjectView3 =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView1, importedProjectView2))
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of("//included_target3.1", "//included_target3.2"),
                    ImmutableList.of()))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView3))
            .bazelPath(dummyBazelPathSection)
            .targets(
                new ProjectViewTargetsSection(
                    ImmutableList.of(
                        "//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    ImmutableList.of("//excluded_target4.1", "//excluded_target4.2")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
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

  // bazelPath

  @Test
  public void shouldReturnEmptyBazelPathForBuilderWithoutBazelPath() {
    // given & when
    var projectView = ProjectView.builder().targets(dummyTargetsSection).build();

    // then
    assertFalse(projectView.getBazelPath().isPresent());
  }

  @Test
  public void shouldReturnEmptyBazelPathForBuilderWithEmptyBazelPath() {
    // given & when
    var projectView =
        ProjectView.builder().targets(dummyTargetsSection).bazelPath(Optional.empty()).build();

    // then
    assertFalse(projectView.getBazelPath().isPresent());
  }

  @Test
  public void shouldReturnEqualBazelPathForBuilderWithoutImports() {
    // given & when
    var projectView =
        ProjectView.builder()
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .build();

    // then
    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());
  }

  @Test
  public void shouldReturnEqualBazelPathForBuilderWithEmptyImports() {
    // given & when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .build();

    // then
    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());
  }

  @Test
  public void shouldReturnImportedBazelPathForBuilderWithoutBazelPath() {
    // given
    var importedProjectView =
        ProjectView.builder()
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView))
            .targets(dummyTargetsSection)
            .build();

    // then
    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());
  }

  @Test
  public void shouldReturnImportedBazelPathForBuilderWithEmptyBazelPath() {
    // given
    var importedProjectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported/path/to/bazel")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView))
            .targets(dummyTargetsSection)
            .bazelPath(Optional.empty())
            .build();

    // then
    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection("imported/path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());
  }

  @Test
  public void shouldReturnCurrentBazelPathForOneImportedProjectView() {
    // given
    var importedProjectView =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported/path/to/bazel")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView))
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .build();

    // then
    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());
  }

  @Test
  public void
      shouldReturnCurrentBazelPathForThreeImportedProjectViewForNotEmptyCurrentProjectView() {
    // given
    var importedProjectView1 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported1/path/to/bazel")))
            .build();

    var importedProjectView2 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported2/path/to/bazel")))
            .build();

    var importedProjectView3 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported3/path/to/bazel")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(
                ImmutableList.of(importedProjectView1, importedProjectView2, importedProjectView3))
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .build();

    // then
    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());
  }

  @Test
  public void shouldReturnLastBazelPathFromNestedImportedProjectViewForEmptyCurrentProjectView() {
    // given
    var importedProjectView1 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported1/path/to/bazel")))
            .build();

    var importedProjectView2 =
        ProjectView.builder()
            .imports(ImmutableList.of())
            .targets(dummyTargetsSection)
            .bazelPath(Optional.empty())
            .build();

    var importedProjectView3 =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView1, importedProjectView2))
            .targets(dummyTargetsSection)
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported3/path/to/bazel")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(ImmutableList.of(importedProjectView3))
            .targets(dummyTargetsSection)
            .bazelPath(Optional.empty())
            .build();

    // then
    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection("imported3/path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());
  }
}
