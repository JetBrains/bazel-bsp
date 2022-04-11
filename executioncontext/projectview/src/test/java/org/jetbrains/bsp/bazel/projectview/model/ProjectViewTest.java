package org.jetbrains.bsp.bazel.projectview.model;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.common.net.HostAndPort;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBazelPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewBuildFlagsSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewJavaPathSection;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProjectViewTest {

  @Nested
  @DisplayName(".builder().imports(..)... tests")
  class BuilderImportsTest {

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
      assertThat(projectViewTry.isFailure()).isTrue();
      assertThat(projectViewTry.getCause().getClass()).isEqualTo(IOException.class);
      assertThat(projectViewTry.getCause().getMessage())
          .isEqualTo("doesnt/exist/projectview2.bazelproject file does not exist!");
    }

    @Test
    public void shouldReturnEmptyValuesForEmptyBuilder() {
      // given & when
      var projectViewTry = ProjectView.builder().build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getTargets()).isEmpty();
      assertThat(projectView.getBazelPath()).isEmpty();
      assertThat(projectView.getDebuggerAddress()).isEmpty();
      assertThat(projectView.getJavaPath()).isEmpty();
      assertThat(projectView.getBuildFlags()).isEmpty();
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
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      assertThat(projectView.getTargets()).isEmpty();
      assertThat(projectView.getBazelPath()).isEmpty();
      assertThat(projectView.getDebuggerAddress()).isEmpty();
      assertThat(projectView.getJavaPath()).isEmpty();
      assertThat(projectView.getBuildFlags()).isEmpty();
    }
  }

  @Nested
  @DisplayName(".builder()... tests")
  class BuilderTest {

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
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag1=value1", "--build_flag2=value2"))))
              .build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
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
      assertThat(projectView.getTargets().get()).isEqualTo(expectedProjectViewTargetsSection);

      var expectedBazelPathSection = new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
      assertThat(projectView.getBazelPath().get()).isEqualTo(expectedBazelPathSection);

      var expectedDebuggerAddressSection =
          new ProjectViewDebuggerAddressSection(HostAndPort.fromString("127.0.0.1:8000"));
      assertThat(projectView.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddressSection);

      var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
      assertThat(projectView.getJavaPath().get()).isEqualTo(expectedJavaPathSection);

      var expectedBuildFlagSection =
          new ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"));
      assertThat(projectView.getBuildFlags().get()).isEqualTo(expectedBuildFlagSection);
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
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag1=value1", "--build_flag2=value2"))))
              .build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
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
      assertThat(projectView.getTargets().get()).isEqualTo(expectedProjectViewTargetsSection);

      var expectedProjectViewBazelPathSection =
          new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
      assertThat(projectView.getBazelPath().get()).isEqualTo(expectedProjectViewBazelPathSection);

      var expectedDebuggerAddressSection =
          new ProjectViewDebuggerAddressSection(HostAndPort.fromString("127.0.0.1:8000"));
      assertThat(projectView.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddressSection);

      var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
      assertThat(projectView.getJavaPath().get()).isEqualTo(expectedJavaPathSection);

      var expectedBuildFlagsSection =
          new ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"));
      assertThat(projectView.getBuildFlags().get()).isEqualTo(expectedBuildFlagsSection);
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
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path/to/java"))))
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag1=value1", "--build_flag2=value2"))))
              .build();

      // when
      var projectViewTry = ProjectView.builder().imports(List.of(importedProjectViewTry)).build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
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
      assertThat(projectView.getTargets().get()).isEqualTo(expectedProjectViewTargetsSection);

      var expectedProjectViewBazelPathSection =
          new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
      assertThat(projectView.getBazelPath().get()).isEqualTo(expectedProjectViewBazelPathSection);

      var expectedDebuggerAddressSection =
          new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"));
      assertThat(projectView.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddressSection);

      var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
      assertThat(projectView.getJavaPath().get()).isEqualTo(expectedJavaPathSection);

      var expectedBuildFlagsSection =
          new ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"));
      assertThat(projectView.getBuildFlags().get()).isEqualTo(expectedBuildFlagsSection);
    }

    @Test
    public void shouldReturnSingletonValuesAndListValuesForEmptyImport() {
      // given
      var importedProjectViewTry = ProjectView.builder().build();

      // when
      var projectViewTry =
          ProjectView.builder()
              .imports(List.of(importedProjectViewTry))
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(new BuildTargetIdentifier("//included_target1")), List.of())))
              .bazelPath(Option.of(new ProjectViewBazelPathSection(Paths.get("path/to/bazel"))))
              .debuggerAddress(
                  Option.of(
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(Option.of(new ProjectViewJavaPathSection(Paths.get("path/to/java"))))
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag1=value1", "--build_flag2=value2"))))
              .build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
      var projectView = projectViewTry.get();

      var expectedProjectViewTargetsSection =
          new ProjectViewTargetsSection(
              List.of(new BuildTargetIdentifier("//included_target1")), List.of());
      assertThat(projectView.getTargets().get()).isEqualTo(expectedProjectViewTargetsSection);

      var expectedProjectViewBazelPathSection =
          new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
      assertThat(projectView.getBazelPath().get()).isEqualTo(expectedProjectViewBazelPathSection);

      var expectedDebuggerAddressSection =
          new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.1:8000"));
      assertThat(projectView.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddressSection);

      var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
      assertThat(projectView.getJavaPath().get()).isEqualTo(expectedJavaPathSection);
      var expectedBuildFlagsSection =
          new ProjectViewBuildFlagsSection(List.of("--build_flag1=value1", "--build_flag2=value2"));
      assertThat(projectView.getBuildFlags().get()).isEqualTo(expectedBuildFlagsSection);
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
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(
                  Option.of(new ProjectViewJavaPathSection(Paths.get("imported/path/to/java"))))
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag1.1=value1.1", "--build_flag1.2=value1.2"))))
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
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag2.1=value2.1", "--build_flag2.2=value2.2"))))
              .build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
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
      assertThat(projectView.getTargets().get()).isEqualTo(expectedProjectViewTargetsSection);

      var expectedProjectViewBazelPathSection =
          new ProjectViewBazelPathSection(Paths.get("path/to/bazel"));
      assertThat(projectView.getBazelPath().get()).isEqualTo(expectedProjectViewBazelPathSection);

      var expectedDebuggerAddressSection =
          new ProjectViewDebuggerAddressSection(HostAndPort.fromString("127.0.0.1:8000"));
      assertThat(projectView.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddressSection);

      var expectedJavaPathSection = new ProjectViewJavaPathSection(Paths.get("path/to/java"));
      assertThat(projectView.getJavaPath().get()).isEqualTo(expectedJavaPathSection);
      var expectedBuildFlagsSection =
          new ProjectViewBuildFlagsSection(
              List.of(
                  "--build_flag1.1=value1.1",
                  "--build_flag1.2=value1.2",
                  "--build_flag2.1=value2.1",
                  "--build_flag2.2=value2.2"));
      assertThat(projectView.getBuildFlags().get()).isEqualTo(expectedBuildFlagsSection);
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
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(
                  Option.of(new ProjectViewJavaPathSection(Paths.get("imported1/path/to/java"))))
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag1.1=value1.1", "--build_flag1.2=value1.2"))))
              .build();

      var importedProjectViewTry2 =
          ProjectView.builder()
              .imports(List.of())
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(new BuildTargetIdentifier("//included_target2.1")),
                          List.of(new BuildTargetIdentifier("//excluded_target2.1")))))
              .buildFlags(
                  Option.of(new ProjectViewBuildFlagsSection(List.of("--build_flag2.1=value2.1"))))
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
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.3:8000"))))
              .javaPath(
                  Option.of(new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"))))
              .buildFlags(
                  Option.of(new ProjectViewBuildFlagsSection(List.of("--build_flag3.1=value3.1"))))
              .build();

      // when
      var projectViewTry =
          ProjectView.builder()
              .imports(
                  List.of(
                      importedProjectViewTry1, importedProjectViewTry2, importedProjectViewTry3))
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
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag4.1=value4.1", "--build_flag4.2=value4.2"))))
              .build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
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
      assertThat(projectView.getTargets().get()).isEqualTo(expectedProjectViewTargetsSection);

      var expectedProjectViewBazelPathSection =
          new ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel"));
      assertThat(projectView.getBazelPath().get()).isEqualTo(expectedProjectViewBazelPathSection);

      var expectedDebuggerAddressSection =
          new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.3:8000"));
      assertThat(projectView.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddressSection);

      var expectedJavaPathSection =
          new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"));
      assertThat(projectView.getJavaPath().get()).isEqualTo(expectedJavaPathSection);
      var expectedBuildFlagsSection =
          new ProjectViewBuildFlagsSection(
              List.of(
                  "--build_flag1.1=value1.1",
                  "--build_flag1.2=value1.2",
                  "--build_flag2.1=value2.1",
                  "--build_flag3.1=value3.1",
                  "--build_flag4.1=value4.1",
                  "--build_flag4.2=value4.2"));
      assertThat(projectView.getBuildFlags().get()).isEqualTo(expectedBuildFlagsSection);
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
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.1:8000"))))
              .javaPath(
                  Option.of(new ProjectViewJavaPathSection(Paths.get("imported1/path/to/java"))))
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag1.1=value1.1", "--build_flag1.2=value1.2"))))
              .build();

      var importedProjectViewTry2 =
          ProjectView.builder()
              .imports(List.of())
              .targets(
                  Option.of(
                      new ProjectViewTargetsSection(
                          List.of(new BuildTargetIdentifier("//included_target2.1")),
                          List.of(new BuildTargetIdentifier("//excluded_target2.1")))))
              .buildFlags(
                  Option.of(new ProjectViewBuildFlagsSection(List.of("--build_flag2.1=value2.1"))))
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
                      new ProjectViewDebuggerAddressSection(
                          HostAndPort.fromString("0.0.0.3:8000"))))
              .javaPath(
                  Option.of(new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"))))
              .buildFlags(
                  Option.of(new ProjectViewBuildFlagsSection(List.of("--build_flag3.1=value3.1"))))
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
              .buildFlags(
                  Option.of(
                      new ProjectViewBuildFlagsSection(
                          List.of("--build_flag4.1=value4.1", "--build_flag4.2=value4.2"))))
              .build();

      // then
      assertThat(projectViewTry.isSuccess()).isTrue();
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
      assertThat(projectView.getTargets().get()).isEqualTo(expectedProjectViewTargetsSection);

      var expectedProjectViewBazelPathSection =
          new ProjectViewBazelPathSection(Paths.get("imported3/path/to/bazel"));
      assertThat(projectView.getBazelPath().get()).isEqualTo(expectedProjectViewBazelPathSection);

      var expectedDebuggerAddressSection =
          new ProjectViewDebuggerAddressSection(HostAndPort.fromString("0.0.0.3:8000"));
      assertThat(projectView.getDebuggerAddress().get()).isEqualTo(expectedDebuggerAddressSection);

      var expectedJavaPathSection =
          new ProjectViewJavaPathSection(Paths.get("imported3/path/to/java"));
      assertThat(projectView.getJavaPath().get()).isEqualTo(expectedJavaPathSection);

      var expectedBuildFlagsSection =
          new ProjectViewBuildFlagsSection(
              List.of(
                  "--build_flag1.1=value1.1",
                  "--build_flag1.2=value1.2",
                  "--build_flag2.1=value2.1",
                  "--build_flag3.1=value3.1",
                  "--build_flag4.1=value4.1",
                  "--build_flag4.2=value4.2"));
      assertThat(projectView.getBuildFlags().get()).isEqualTo(expectedBuildFlagsSection);
    }
  }
}
