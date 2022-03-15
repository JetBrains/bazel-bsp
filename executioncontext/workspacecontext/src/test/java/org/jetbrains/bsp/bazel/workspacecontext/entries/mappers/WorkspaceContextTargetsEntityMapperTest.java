package org.jetbrains.bsp.bazel.workspacecontext.entries.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.executioncontext.api.entries.mappers.ProjectViewToExecutionContextEntityMapperException;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;
import org.junit.Before;
import org.junit.Test;

public class WorkspaceContextTargetsEntityMapperTest {

  private WorkspaceContextTargetsEntityMapper mapper;

  @Before
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
    assertTrue(targetsTry.isFailure());
    assertEquals(
        ProjectViewToExecutionContextEntityMapperException.class, targetsTry.getCause().getClass());
    assertEquals(
        "Mapping project view into 'targets' failed! 'targets' section in project view is empty.",
        targetsTry.getCause().getMessage());
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
    assertTrue(targetsTry.isFailure());
    assertEquals(
        ProjectViewToExecutionContextEntityMapperException.class, targetsTry.getCause().getClass());
    assertEquals(
        "Mapping project view into 'targets' failed! 'targets' section has no included targets.",
        targetsTry.getCause().getMessage());
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
    assertTrue(targetsTry.isSuccess());
    var targets = targetsTry.get();

    var expectedTargets =
        new ExecutionContextTargetsEntity(
            io.vavr.collection.List.of(
                new BuildTargetIdentifier("//included_target1"),
                new BuildTargetIdentifier("//included_target2"),
                new BuildTargetIdentifier("//included_target3")),
            io.vavr.collection.List.of(
                new BuildTargetIdentifier("//excluded_target1"),
                new BuildTargetIdentifier("//excluded_target2")));
    assertEquals(expectedTargets, targets);
  }
}
