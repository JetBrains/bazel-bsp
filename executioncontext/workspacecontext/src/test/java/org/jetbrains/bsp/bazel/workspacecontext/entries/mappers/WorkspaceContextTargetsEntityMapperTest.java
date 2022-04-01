package org.jetbrains.bsp.bazel.workspacecontext.entries.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapperException;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkspaceContextTargetsEntityMapperTest {

  private WorkspaceContextTargetsEntityMapper mapper;

  @BeforeEach
  public void beforeEach() {
    // given
    this.mapper = new WorkspaceContextTargetsEntityMapper();
  }

  @Test
  public void shouldReturnFailIfTargetsAreEmpty() {
    // given
    var projectView = ProjectView.builder().targets(Option.none()).build().get();

    // when
    var targetsTry = mapper.map(projectView);

    // then
    assertThat(targetsTry.isFailure()).isTrue();
    assertThat(targetsTry.getCause().getClass())
        .isEqualTo(ProjectViewToExecutionContextEntityMapperException.class);
    assertThat(targetsTry.getCause().getMessage())
        .isEqualTo(
            "Mapping project view into 'targets' failed! 'targets' section in project view is"
                + " empty.");
  }

  @Test
  public void shouldReturnFailIfTargetsHaveNoIncludedValues() {
    // given
    var projectView =
        ProjectView.builder()
            .targets(
                Option.of(
                    new ProjectViewTargetsSection(
                        List.of(),
                        List.of(
                            new BuildTargetIdentifier("//excluded_target1"),
                            new BuildTargetIdentifier("//excluded_target2")))))
            .build()
            .get();

    // when
    var targetsTry = mapper.map(projectView);

    // then
    assertThat(targetsTry.isFailure()).isTrue();
    assertThat(targetsTry.getCause().getClass())
        .isEqualTo(ProjectViewToExecutionContextEntityMapperException.class);
    assertThat(targetsTry.getCause().getMessage())
        .isEqualTo(
            "Mapping project view into 'targets' failed! 'targets' section has no included"
                + " targets.");
  }

  @Test
  public void shouldReturnSuccessForSuccessfulMapping() {
    // given
    var projectView =
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
            .build()
            .get();

    // when
    var targetsTry = mapper.map(projectView);

    // then
    assertThat(targetsTry.isSuccess()).isTrue();
    var targets = targetsTry.get();

    var expectedTargets =
        new ExecutionContextTargetsEntity(
            List.of(
                new BuildTargetIdentifier("//included_target1"),
                new BuildTargetIdentifier("//included_target2"),
                new BuildTargetIdentifier("//included_target3")),
            List.of(
                new BuildTargetIdentifier("//excluded_target1"),
                new BuildTargetIdentifier("//excluded_target2")));
    assertThat(targets).isEqualTo(expectedTargets);
  }
}
