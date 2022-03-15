package org.jetbrains.bsp.bazel.projectview.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.net.HostAndPort;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.Test;

public class ProjectViewTest {

  // imports specific tests

  @Test
  public void shouldReturnFailureWithFirstCauseForBuilderWithFailureImports() {
    // given
    var importedProjectViewTry1 = ProjectView.builder().build();

    var importedProjectViewTry2 =
        Try.<ProjectView>failure(
            new IOException("doesnt/exist/projectview2.bazelproject file does not exist!"));

    var importedProjectViewTry3 = ProjectView.builder().imports(List.of()).build();

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
            .build();

    // then
    assertTrue(projectViewTry.isFailure());
    assertEquals(
        "doesnt/exist/projectview2.bazelproject file does not exist!",
        projectViewTry.getCause().getMessage());
  }

  @Test
  public void shouldReturnEmptyValuesForEmptyBuilder() {
    // given & when
    var projectViewTry = ProjectView.builder().build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getTargets().isEmpty());
    assertTrue(projectView.getBazelPath().isEmpty());
    assertTrue(projectView.getDebuggerAddress().isEmpty());
    assertTrue(projectView.getJavaPath().isEmpty());
  }

  @Test
  public void shouldReturnEmptyValuesForBuilderWithEmptyValues() {
    // given & when
    var projectViewTry =
        ProjectView.builder()
            .targets(Option.none())
            .bazelPath(Option.none())
            .debuggerAddress(Option.none())
            .javaPath(Option.none())
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    assertTrue(projectView.getTargets().isEmpty());
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
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target1"),
                            new BuildTargetIdentifier("//included_target2"),
                            new BuildTargetIdentifier("//included_target3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target1"),
                            new BuildTargetIdentifier("//excluded_target2")))))
            .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(
                        HostAndPort.fromString("127.0.0.1:8000"))))
            .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path/to/java"))))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                new BuildTargetIdentifier("//included_target1"),
                new BuildTargetIdentifier("//included_target2"),
                new BuildTargetIdentifier("//included_target3")),
            List.of(
                new BuildTargetIdentifier("//excluded_target1"),
                new BuildTargetIdentifier("//excluded_target2")));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets().get());

    var expectedBazelPathSection = new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
    assertEquals(expectedBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection =
        new ProjectViewDebuggerAddressSection(HostAndPort.fromString("127.0.0.1:8000"));
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldBuildProjectViewWithEmptyImports() {
    // given & when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target1"),
                            new BuildTargetIdentifier("//included_target2"),
                            new BuildTargetIdentifier("//included_target3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target1"),
                            new BuildTargetIdentifier("//excluded_target2")))))
            .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(
                        HostAndPort.fromString("127.0.0.1:8000"))))
            .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path/to/java"))))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                new BuildTargetIdentifier("//included_target1"),
                new BuildTargetIdentifier("//included_target2"),
                new BuildTargetIdentifier("//included_target3")),
            List.of(
                new BuildTargetIdentifier("//excluded_target1"),
                new BuildTargetIdentifier("//excluded_target2")));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets().get());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection =
        new ProjectViewDebuggerAddressSection(HostAndPort.fromString("127.0.0.1:8000"));
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnImportedSingletonValuesAndListValues() {
    // given
    var importedProjectViewTry =
        ProjectView.builder()
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target1.1"),
                            new BuildTargetIdentifier("//included_target1.2"),
                            new BuildTargetIdentifier("//included_target1.3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target1.1"),
                            new BuildTargetIdentifier("//excluded_target1.2")))))
            .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"))))
            .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path/to/java"))))
            .build();

    // when
    var projectViewTry = ProjectView.builder().imports(List.of(importedProjectViewTry)).build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                new BuildTargetIdentifier("//included_target1.1"),
                new BuildTargetIdentifier("//included_target1.2"),
                new BuildTargetIdentifier("//included_target1.3")),
            List.of(
                new BuildTargetIdentifier("//excluded_target1.1"),
                new BuildTargetIdentifier("//excluded_target1.2")));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets().get());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection =
        new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"));
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnSingletonValuesAndListValuesForEmptyImport() {
    // given
    var importedProjectViewTry = ProjectView.builder().build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(new BuildTargetIdentifier("//included_target1")), List.of())))
            .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"))))
            .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path/to/java"))))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(new BuildTargetIdentifier("//included_target1")), List.of());
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets().get());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection =
        new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"));
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnCurrentSingletonValuesAndCombinedListValues() {
    // given
    var importedProjectViewTry =
        ProjectView.builder()
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target1.1"),
                            new BuildTargetIdentifier("//included_target1.2"),
                            new BuildTargetIdentifier("//included_target1.3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target1.1"),
                            new BuildTargetIdentifier("//excluded_target1.2")))))
            .bazelPath(
                Option.of(new ProjectViewBazelPathSection(Paths.get("imported/path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"))))
            .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("imported/path/to/java"))))
            .build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of(importedProjectViewTry))
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target2.1"),
                            new BuildTargetIdentifier("//included_target2.2"),
                            new BuildTargetIdentifier("//included_target2.3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target2.1"),
                            new BuildTargetIdentifier("//excluded_target2.2")))))
            .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(
                        HostAndPort.fromString("127.0.0.1:8000"))))
            .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path/to/java"))))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                new BuildTargetIdentifier("//included_target1.1"),
                new BuildTargetIdentifier("//included_target1.2"),
                new BuildTargetIdentifier("//included_target1.3"),
                new BuildTargetIdentifier("//included_target2.1"),
                new BuildTargetIdentifier("//included_target2.2"),
                new BuildTargetIdentifier("//included_target2.3")),
            List.of(
                new BuildTargetIdentifier("//excluded_target1.1"),
                new BuildTargetIdentifier("//excluded_target1.2"),
                new BuildTargetIdentifier("//excluded_target2.1"),
                new BuildTargetIdentifier("//excluded_target2.2")));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets().get());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection =
        new ProjectViewDebuggerAddressSection(HostAndPort.fromString("127.0.0.1:8000"));
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnLastSingletonValuesAndCombinedListValuesForThreeImports() {
    // given
    var importedProjectViewTry1 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target1.1"),
                            new BuildTargetIdentifier("//included_target1.2"),
                            new BuildTargetIdentifier("//included_target1.3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target1.1"),
                            new BuildTargetIdentifier("//excluded_target1.2")))))
            .bazelPath(
                Option.of(new ProjectViewBazelPathSection(Paths.get("imported1/path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"))))
            .javaPath(
                Option.of(new ProjectViewJavaPathSection(Paths.get("imported1/path/to/java"))))
            .build();

    var importedProjectViewTry2 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(new BuildTargetIdentifier("//included_target2.1")),
                        List.of(new BuildTargetIdentifier("//excluded_target2.1")))))
            .build();

    var importedProjectViewTry3 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target3.1"),
                            new BuildTargetIdentifier("//included_target3.2")),
                        List.of())))
            .bazelPath(
                Option.of(new ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.3:8000"))))
            .javaPath(
                Option.of(new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"))))
            .build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(
                List.of(importedProjectViewTry1, importedProjectViewTry2, importedProjectViewTry3))
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target4.1"),
                            new BuildTargetIdentifier("//included_target4.2"),
                            new BuildTargetIdentifier("//included_target4.3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target4.1"),
                            new BuildTargetIdentifier("//excluded_target4.2")))))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                new BuildTargetIdentifier("//included_target1.1"),
                new BuildTargetIdentifier("//included_target1.2"),
                new BuildTargetIdentifier("//included_target1.3"),
                new BuildTargetIdentifier("//included_target2.1"),
                new BuildTargetIdentifier("//included_target3.1"),
                new BuildTargetIdentifier("//included_target3.2"),
                new BuildTargetIdentifier("//included_target4.1"),
                new BuildTargetIdentifier("//included_target4.2"),
                new BuildTargetIdentifier("//included_target4.3")),
            List.of(
                new BuildTargetIdentifier("//excluded_target1.1"),
                new BuildTargetIdentifier("//excluded_target1.2"),
                new BuildTargetIdentifier("//excluded_target2.1"),
                new BuildTargetIdentifier("//excluded_target4.1"),
                new BuildTargetIdentifier("//excluded_target4.2")));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets().get());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel"));
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection =
        new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.3:8000"));
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection =
        new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"));
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }

  @Test
  public void shouldReturnLastSingletonValuesAndCombinedListValuesForNestedImports() {
    // given
    var importedProjectViewTry1 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target1.1"),
                            new BuildTargetIdentifier("//included_target1.2"),
                            new BuildTargetIdentifier("//included_target1.3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target1.1"),
                            new BuildTargetIdentifier("//excluded_target1.2")))))
            .bazelPath(
                Option.of(new ProjectViewBazelPathSection(Paths.get("imported1/path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"))))
            .javaPath(
                Option.of(new ProjectViewJavaPathSection(Paths.get("imported1/path/to/java"))))
            .build();

    var importedProjectViewTry2 =
        ProjectView.builder()
            .imports(List.of())
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(new BuildTargetIdentifier("//included_target2.1")),
                        List.of(new BuildTargetIdentifier("//excluded_target2.1")))))
            .build();

    var importedProjectViewTry3 =
        ProjectView.builder()
            .imports(List.of(importedProjectViewTry1, importedProjectViewTry2))
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target3.1"),
                            new BuildTargetIdentifier("//included_target3.2")),
                        List.of())))
            .bazelPath(
                Option.of(new ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel"))))
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.3:8000"))))
            .javaPath(
                Option.of(new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"))))
            .build();

    var importedProjectViewTry4 = ProjectView.builder().build();

    // when
    var projectViewTry =
        ProjectView.builder()
            .imports(List.of(importedProjectViewTry3, importedProjectViewTry4))
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(
                            new BuildTargetIdentifier("//included_target4.1"),
                            new BuildTargetIdentifier("//included_target4.2"),
                            new BuildTargetIdentifier("//included_target4.3")),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target4.1"),
                            new BuildTargetIdentifier("//excluded_target4.2")))))
            .build();

    // then
    assertTrue(projectViewTry.isSuccess());
    var projectView = projectViewTry.get();

    var expectedProjectViewTargetsSection =
        new ProjectViewTargetsSection(
            List.of(
                new BuildTargetIdentifier("//included_target1.1"),
                new BuildTargetIdentifier("//included_target1.2"),
                new BuildTargetIdentifier("//included_target1.3"),
                new BuildTargetIdentifier("//included_target2.1"),
                new BuildTargetIdentifier("//included_target3.1"),
                new BuildTargetIdentifier("//included_target3.2"),
                new BuildTargetIdentifier("//included_target4.1"),
                new BuildTargetIdentifier("//included_target4.2"),
                new BuildTargetIdentifier("//included_target4.3")),
            List.of(
                new BuildTargetIdentifier("//excluded_target1.1"),
                new BuildTargetIdentifier("//excluded_target1.2"),
                new BuildTargetIdentifier("//excluded_target2.1"),
                new BuildTargetIdentifier("//excluded_target4.1"),
                new BuildTargetIdentifier("//excluded_target4.2")));
    assertEquals(expectedProjectViewTargetsSection, projectView.getTargets().get());

    var expectedProjectViewBazelPathSection =
        new ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel"));
    assertEquals(expectedProjectViewBazelPathSection, projectView.getBazelPath().get());

    var expectedDebuggerAddressSection =
        new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.3:8000"));
    assertEquals(expectedDebuggerAddressSection, projectView.getDebuggerAddress().get());

    var expectedJavaPathSection =
        new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"));
    assertEquals(expectedJavaPathSection, projectView.getJavaPath().get());
  }
}
