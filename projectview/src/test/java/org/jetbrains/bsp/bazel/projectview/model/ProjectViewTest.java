package org.jetbrains.bsp.bazel.projectview.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.vavr.control.Try;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.Test;

public class ProjectViewTest {

  private static final ProjectViewTargetsSection dummyTargetsSection =
      new ProjectViewTargetsSection(List.of("//dummy_included_target"), List.of());
  private final Optional<ProjectViewBazelPathSection> dummyBazelPathSection = Optional.empty();
  private final Optional<ProjectViewDebuggerAddressSection> dummyDebuggerAddress = Optional.empty();
  private final Optional<ProjectViewJavaPathSection> dummyJavaPath = Optional.empty();

  // imports specific tests

  @Test
  public void shouldReturnFailureWithFirstCauseForBuilderWithFailureImports() {
    // given
    var importedProjectViewTry1 =
        ProjectView.builder()
            .targets(dummyTargetsSection)
            .bazelPath(dummyBazelPathSection)
            .debuggerAddress(dummyDebuggerAddress)
            .javaPath(dummyJavaPath)
            .build();

    var importedProjectViewTry2 =
        Try.<ProjectView>failure(
            new IOException("doesnt/exist/projectview2.bazelproject file does not exist!"));

    var importedProjectViewTry3 =
        ProjectView.builder()
            .imports(List.of())
            .targets(dummyTargetsSection)
            .bazelPath(dummyBazelPathSection)
            .debuggerAddress(dummyDebuggerAddress)
            .javaPath(dummyJavaPath)
            .build();

    var importedProjectViewTry4 =
        Try.<ProjectView>failure(
            new IOException("doesnt/exist/projectview4.bazelproject file does not exist!"));

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(
                List.of(
                    importedProjectViewTry1,
                    importedProjectViewTry2,
                    importedProjectViewTry3,
                    importedProjectViewTry4))
            .targets(dummyTargetsSection)
            .bazelPath(dummyBazelPathSection)
            .debuggerAddress(dummyDebuggerAddress)
            .javaPath(dummyJavaPath)
            .build();

    // then
    assertTrue(projectViewTry.isFailure());
    assertEquals(
        "doesnt/exist/projectview2.bazelproject file does not exist!",
        projectViewTry.getCause().getMessage());
  }

  // targets specific tests

  @Test
  public void shouldReturnEmptyTargetsSectionForBuilderWithoutTargets() {
    // given & when
    var projectViewTry =
        ProjectView.builder()
            .bazelPath(dummyBazelPathSection)
            .debuggerAddress(dummyDebuggerAddress)
            .javaPath(dummyJavaPath)
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedTargetsSection = new ProjectViewTargetsSection(List.of(), List.of());
    assertEquals(expectedTargetsSection, projectView.getTargets());
  }

  // singleton values specific tests

  @Test
  public void shouldReturnEmptySingletonValuesForEmptyBuilder() {
    // given & when
    var projectViewTry = ProjectView.builder().targets(dummyTargetsSection).build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getBazelPath().isEmpty());
    assertTrue(projectView.getDebuggerAddress().isEmpty());
    assertTrue(projectView.getJavaPath().isEmpty());
  }

  @Test
  public void shouldReturnEmptySingletonValuesForBuilderWithEmptyValues() {
    // given & when
    var projectViewTry =
        ProjectView.builder()
            .targets(dummyTargetsSection)
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getBazelPath().isEmpty());
    assertTrue(projectView.getDebuggerAddress().isEmpty());
    assertTrue(projectView.getJavaPath().isEmpty());
  }

  // project view general tests

  @Test
  public void shouldBuildProjectViewWithoutImports() {
    // given & when
    var projectViewTry =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1", "//included_target2", "//included_target3"),
                    List.of("//excluded_target1", "//excluded_target2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("127.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path/to/java")))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of("//included_target1", "//included_target2", "//included_target3"),
            List.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("127.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection("path/to/java");
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldBuildProjectViewWithEmptyImports() {
    // given & when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1", "//included_target2", "//included_target3"),
                    List.of("//excluded_target1", "//excluded_target2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("127.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path/to/java")))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of("//included_target1", "//included_target2", "//included_target3"),
            List.of("//excluded_target1", "//excluded_target2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("127.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection("path/to/java");
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnImportedSingletonValuesAndListValues() {
    // given
    var importedProjectViewTry =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path/to/java")))
            .build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of(importedProjectViewTry))
            .targets(new ProjectViewTargetsSection())
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
            List.of("//excluded_target1.1", "//excluded_target1.2"));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("0.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection("path/to/java");
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnSingletonValuesAndListValuesForEmptyImport() {
    // given
    var importedProjectViewTry =
        ProjectView.builder()
            .targets(new ProjectViewTargetsSection())
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of())
            .targets(new ProjectViewTargetsSection(List.of(), List.of()))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path/to/java")))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection = new ProjectViewTargetsSection(List.of(), List.of());
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets());

    var expectedProjectViewBazelPathSection = new ProjectViewBazelPathSection("path/to/bazel");
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection = new ProjectViewDebuggerAddressSection("0.0.0.1:8000");
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection("path/to/java");
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnCurrentSingletonValuesAndCombinedListValues() {
    // given
    var importedProjectViewTry =
        ProjectView.builder()
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("imported/path/to/java")))
            .build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of(importedProjectViewTry))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target2.1", "//included_target2.2", "//included_target2.3"),
                    List.of("//excluded_target2.1", "//excluded_target2.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("127.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("path/to/java")))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

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

    var expectedJavaPathSection = new ProjectViewJavaPathSection("path/to/java");
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnLastSingletonValuesAndCombinedListValuesForThreeImports() {
    // given
    var importedProjectViewTry1 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported1/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("imported1/path/to/java")))
            .build();

    var importedProjectViewTry2 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target2.1"), List.of("//excluded_target2.1")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build();

    var importedProjectViewTry3 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target3.1", "//included_target3.2"), List.of()))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported3/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.3:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("imported3/path/to/java")))
            .build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(
                List.of(importedProjectViewTry1, importedProjectViewTry2, importedProjectViewTry3))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    List.of("//excluded_target4.1", "//excluded_target4.2")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

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

    var expectedJavaPathSection = new ProjectViewJavaPathSection("imported3/path/to/java");
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnLastSingletonValuesAndCombinedListValuesForNestedImports() {
    // given
    var importedProjectViewTry1 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target1.1", "//included_target1.2", "//included_target1.3"),
                    List.of("//excluded_target1.1", "//excluded_target1.2")))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported1/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.1:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("imported1/path/to/java")))
            .build();

    var importedProjectViewTry2 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target2.1"), List.of("//excluded_target2.1")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build();

    var importedProjectViewTry3 =
        ProjectView.builder()
            .imports(List.of(importedProjectViewTry1, importedProjectViewTry2))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target3.1", "//included_target3.2"), List.of()))
            .bazelPath(Optional.of(new ProjectViewBazelPathSection("imported3/path/to/bazel")))
            .debuggerAddress(Optional.of(new ProjectViewDebuggerAddressSection("0.0.0.3:8000")))
            .javaPath(Optional.of(new ProjectViewJavaPathSection("imported3/path/to/java")))
            .build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of(importedProjectViewTry3))
            .targets(
                new ProjectViewTargetsSection(
                    List.of("//included_target4.1", "//included_target4.2", "//included_target4.3"),
                    List.of("//excluded_target4.1", "//excluded_target4.2")))
            .bazelPath(Optional.empty())
            .debuggerAddress(Optional.empty())
            .javaPath(Optional.empty())
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

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

    var expectedJavaPathSection = new ProjectViewJavaPathSection("imported3/path/to/java");
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }
}
