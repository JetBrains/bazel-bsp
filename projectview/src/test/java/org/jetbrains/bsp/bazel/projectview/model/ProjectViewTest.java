package org.jetbrains.bsp.bazel.projectview.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.Test;

public class ProjectViewTest {

  private static final ProjectViewTargetsSection dummyTargetsSection =
      new ProjectViewTargetsSection(List.of("//dummy_included_target"), List.of());
  private final Optional<ProjectViewBazelPathSection> dummyBazelPathSection = Optional.empty();
  private final Optional<ProjectViewDebuggerAddressSection> dummyDebuggerAddress = Optional.empty();

  // targets specific tests

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForBuilderWithoutTargets() {
    // given & when
    ProjectView.builder()
        .bazelPath(dummyBazelPathSection)
        .debuggerAddress(dummyDebuggerAddress)
        .build();

    // then
    // throw an exception
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionForBuilderWithoutIncludedTargets() {
    // given & when
    ProjectView.builder()
        .targets(new ProjectViewTargetsSection(List.of(), List.of("//excluded_target")))
        .bazelPath(dummyBazelPathSection)
        .debuggerAddress(dummyDebuggerAddress)
        .build();

    // then
    // throw an exception
  }

  // singleton values specific tests

  @Test
  public void shouldReturnEmptySingletonValuesForEmptyBuilder() {
    // given & when
    var projectView = ProjectView.builder().targets(dummyTargetsSection).build();

    // then
    assertFalse(projectView.getBazelPath().isPresent());
    assertFalse(projectView.getDebuggerAddress().isPresent());
  }

  @Test
  public void shouldReturnEmptySingletonValuesForBuilderWithEmptyValues() {
    // given & when
    var projectView =
        ProjectView.builder()
            .targets(dummyTargetsSection)
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .build();

    // then
    assertFalse(projectView.getBazelPath().isPresent());
    assertFalse(projectView.getDebuggerAddress().isPresent());
  }

  // project view general tests

  @Test
  public void shouldBuildProjectViewWithoutImports() {
    // given & when
    var projectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1", "//included_target2", "//included_target3"),
                    List.of("//excluded_target1", "//excluded_target2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("127.0.0.1:8000")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of("//included_target1", "//included_target2", "//included_target3"),
            List.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("127.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());
  }

  @Test
  public void shouldBuildProjectViewWithEmptyImports() {
    // given & when
    var projectView =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1", "//included_target2", "//included_target3"),
                    List.of("//excluded_target1", "//excluded_target2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("127.0.0.1:8000")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of("//included_target1", "//included_target2", "//included_target3"),
            List.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("127.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());
  }

  @Test
  public void shouldReturnImportedSingletonValuesAndListValues() {
    // given
    var importedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(List.of(importedProjectView))
            .targets(new ProjectViewTargetsSection())
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
            List.of("//excluded_target1.1", "//excluded_target1.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("0.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());
  }

  @Test
  public void shouldReturnCurrentSingletonValuesAndCombinedListValues() {
    // given
    var importedProjectView =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(List.of(importedProjectView))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target2.1", "//included_target2.2", "//included_target2.3"),
                    List.of("//excluded_target2.1", "//excluded_target2.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("127.0.0.1:8000")))
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                "//included_target1.1",
                "//included_target1.2",
                "//included_target1.3",
                "//included_target2.1",
                "//included_target2.2",
                "//included_target2.3"),
            List.of(
                "//excluded_target1.1",
                "//excluded_target1.2",
                "//excluded_target2.1",
                "//excluded_target2.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("127.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());
  }

  @Test
  public void shouldReturnLastSingletonValuesAndCombinedListValuesForThreeImports() {
    // given
    var importedProjectView1 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported1/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();

    var importedProjectView2 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target2.1"), List.of("//excluded_target2.1")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .build();

    var importedProjectView3 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target3.1", "//included_target3.2"), List.of()))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported3/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.3:8000")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(List.of(importedProjectView1, importedProjectView2, importedProjectView3))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    List.of("//excluded_target4.1", "//excluded_target4.2")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                "//included_target1.1",
                "//included_target1.2",
                "//included_target1.3",
                "//included_target2.1",
                "//included_target3.1",
                "//included_target3.2",
                "//included_target4.1",
                "//included_target4.2",
                "//included_target4.3"),
            List.of(
                "//excluded_target1.1",
                "//excluded_target1.2",
                "//excluded_target2.1",
                "//excluded_target4.1",
                "//excluded_target4.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection("imported3/path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("0.0.0.3:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());
  }

  @Test
  public void shouldReturnLastSingletonValuesAndCombinedListValuesForNestedImports() {
    // given
    var importedProjectView1 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported1/path/to/bazel")))
                .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .build();

    var importedProjectView2 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target2.1"), List.of("//excluded_target2.1")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .build();

    var importedProjectView3 =
        ProjectView.builder()
            .imports(List.of(importedProjectView1, importedProjectView2))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target3.1", "//included_target3.2"), List.of()))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported3/path/to/bazel")))
                .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.3:8000")))
            .build();

    // when
    var projectView =
        ProjectView.builder()
            .imports(List.of(importedProjectView3))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    List.of("//excluded_target4.1", "//excluded_target4.2")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .build();

    // then
    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                "//included_target1.1",
                "//included_target1.2",
                "//included_target1.3",
                "//included_target2.1",
                "//included_target3.1",
                "//included_target3.2",
                "//included_target4.1",
                "//included_target4.2",
                "//included_target4.3"),
            List.of(
                "//excluded_target1.1",
                "//excluded_target1.2",
                "//excluded_target2.1",
                "//excluded_target4.1",
                "//excluded_target4.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection("imported3/path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("0.0.0.3:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());
  }
}
