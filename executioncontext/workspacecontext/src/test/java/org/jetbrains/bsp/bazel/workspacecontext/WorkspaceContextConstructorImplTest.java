package org.jetbrains.bsp.bazel.workspacecontext;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewTargetsSection;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class WorkspaceContextConstructorImplTest {

  private WorkspaceContextConstructorImpl workspaceContextConstructor;

  @BeforeEach
  public void beforeEach() {
    // given
    this.workspaceContextConstructor = new WorkspaceContextConstructorImpl();
  }

  @Nested
  @DisplayName("Try<WorkspaceContext> construct(Try<ProjectView> projectViewTry) tests")
  class ConstructTryTest {

    @Test
    public void shouldReturnFailureIfProjectViewIsFailure() {
      // given
      var projectViewTry = Try.<ProjectView>failure(new Exception("exception message"));

      // when
      var workspaceContextTry = workspaceContextConstructor.construct(projectViewTry);

      // then
      assertThat(workspaceContextTry.isFailure()).isTrue();
      assertThat(workspaceContextTry.getCause().getClass()).isEqualTo(Exception.class);
      assertThat(workspaceContextTry.getCause().getMessage()).isEqualTo("exception message");
    }
  }

  @Nested
  @DisplayName("Try<WorkspaceContext> construct(ProjectView projectView) tests")
  class ConstructTest {

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
      assertThat(workspaceContextTry.isSuccess()).isTrue();
      var workspaceContext = workspaceContextTry.get();

      var expectedTargets =
          new ExecutionContextTargetsEntity(
              List.of(
                  new BuildTargetIdentifier("//included_target1"),
                  new BuildTargetIdentifier("//included_target2"),
                  new BuildTargetIdentifier("//included_target3")),
              List.of(new BuildTargetIdentifier("//excluded_target1")));
      assertThat(workspaceContext.getTargets()).isEqualTo(expectedTargets);
    }
  }
}
