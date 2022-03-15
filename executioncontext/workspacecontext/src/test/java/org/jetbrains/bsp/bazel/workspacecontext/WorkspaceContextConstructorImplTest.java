package org.jetbrains.bsp.bazel.workspacecontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;
import org.junit.Before;
import org.junit.Test;

public class WorkspaceContextConstructorImplTest {

  private WorkspaceContextConstructorImpl workspaceContextConstructor;

  @Before
  public void beforeEach() {
    // given
    this.workspaceContextConstructor = new WorkspaceContextConstructorImpl();
  }

  // Try<WorkspaceContext> construct(Try<ProjectView> projectViewTry)

  @Test
  public void shouldReturnFailureIfProjectViewIsFailure() {
    // given
    var projectViewTry = Try.<ProjectView>failure(new Exception("exception message"));

    // when
    var workspaceContextTry = workspaceContextConstructor.construct(projectViewTry);

    // then
    assertTrue(workspaceContextTry.isFailure());
    assertEquals(Exception.class, workspaceContextTry.getCause().getClass());
    assertEquals("exception message", workspaceContextTry.getCause().getMessage());
  }

  // Try<WorkspaceContext> construct(ProjectView projectView)

  @Test
  public void shouldReturnSuccessIfProjectViewIsValid() {
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
                        List.of(new BuildTargetIdentifier("//excluded_target1")))))
            .build();

    // when
    var workspaceContextTry = workspaceContextConstructor.construct(projectView);

    // then
    assertTrue(workspaceContextTry.isSuccess());
    var workspaceContext = workspaceContextTry.get();

    var expectedTargets =
        new ExecutionContextTargetsEntity(
            io.vavr.collection.List.of(
                new BuildTargetIdentifier("//included_target1"),
                new BuildTargetIdentifier("//included_target2"),
                new BuildTargetIdentifier("//included_target3")),
            io.vavr.collection.List.of(new BuildTargetIdentifier("//excluded_target1")));
    assertEquals(expectedTargets, workspaceContext.getTargets());
  }
}
