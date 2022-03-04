package org.jetbrains.bsp.bazel.workspacecontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import io.vavr.collection.List;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;
import org.junit.Test;

public class WorkspaceContextTest {

  @Test
  public void shouldReturnFailureIfTargetsAreNotDefined() {
    // given & when
    var workspaceContextTry = WorkspaceContext.builder().build();

    // then
    assertTrue(workspaceContextTry.isFailure());
    assertEquals(IllegalStateException.class, workspaceContextTry.getCause().getClass());
    assertEquals(
        "Workspace context creation failed! 'targets' has to be defined.",
        workspaceContextTry.getCause().getMessage());
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
    assertTrue(workspaceContextTry.isSuccess());
    var workspaceContext = workspaceContextTry.get();

    var expectedTargets =
        new ExecutionContextTargetsEntity(
            List.of(
                new BuildTargetIdentifier("//included_target1"),
                new BuildTargetIdentifier("//included_target2")),
            List.of(new BuildTargetIdentifier("//excluded_target1")));
    assertEquals(expectedTargets, workspaceContext.getTargets());
  }
}
