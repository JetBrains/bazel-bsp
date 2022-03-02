package org.jetbrains.bsp.bazel.workspacecontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.vavr.control.Try;
import java.util.Optional;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
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
  public void shouldReturnFailureIfTargetsAreEmptyInProjectView() {
    // given
    var projectView = ProjectView.builder().targets(Optional.empty()).build();

    // when
    var workspaceContextTry = workspaceContextConstructor.construct(projectView);

    // then
    //    assertTrue(workspaceContextTry.isFailure());
  }
}
