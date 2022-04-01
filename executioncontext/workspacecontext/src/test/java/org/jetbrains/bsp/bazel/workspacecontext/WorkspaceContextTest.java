package org.jetbrains.bsp.bazel.workspacecontext;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;
import org.junit.jupiter.api.Test;

public class WorkspaceContextTest {

  @Test
  public void shouldReturnFailureIfTargetsAreNotDefined() {
    // given & when
    var workspaceContextTry = WorkspaceContext.builder().build();

    // then
    assertThat(workspaceContextTry.isFailure()).isTrue();
    assertThat(workspaceContextTry.getCause().getClass()).isEqualTo(IllegalStateException.class);
    assertThat(workspaceContextTry.getCause().getMessage())
        .isEqualTo("Workspace context creation failed! 'targets' has to be defined.");
  }

  @Test
  public void shouldReturnSuccessForIfAllFieldsAreDefined() {
    // given & when
    var workspaceContextTry =
        WorkspaceContext.builder()
            .targets(
                new ExecutionContextTargetsEntity(
                    List.of(
                        new BuildTargetIdentifier("//included_target1"),
                        new BuildTargetIdentifier("//included_target2")),
                    List.of(new BuildTargetIdentifier("//excluded_target1"))))
            .build();

    // then
    assertThat(workspaceContextTry.isSuccess()).isTrue();
    var workspaceContext = workspaceContextTry.get();

    var expectedTargets =
        new ExecutionContextTargetsEntity(
            List.of(
                new BuildTargetIdentifier("//included_target1"),
                new BuildTargetIdentifier("//included_target2")),
            List.of(new BuildTargetIdentifier("//excluded_target1")));
    assertThat(workspaceContext.getTargets()).isEqualTo(expectedTargets);
  }
}
