package org.jetbrains.bsp.bazel.workspacecontext;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;

/**
 * Representation of <code>ExecutionContext</code> used during server lifetime.
 *
 * @see org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext
 */
public class WorkspaceContext extends ExecutionContext {

  /**
   * Targets (included and excluded) on which the user wants to work.
   *
   * <p>Obtained from <code>ProjectView</code> simply by mapping 'targets' section.
   */
  private final ExecutionContextTargetsEntity targets;

  private WorkspaceContext(ExecutionContextTargetsEntity targets) {
    this.targets = targets;
  }

  public ExecutionContextTargetsEntity getTargets() {
    return targets;
  }

  public static WorkspaceContext.Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private static final Logger log = LogManager.getLogger(WorkspaceContext.Builder.class);

    private Option<ExecutionContextTargetsEntity> targets = Option.none();

    private Builder() {}

    public Builder targets(ExecutionContextTargetsEntity targets) {
      this.targets = Option.of(targets);

      return this;
    }

    public Try<WorkspaceContext> build() {
      if (targets.isEmpty()) {
        var exceptionMessage = "Workspace context creation failed! 'targets' has to be defined.";

        log.error(exceptionMessage);

        return Try.failure(new IllegalStateException(exceptionMessage));
      }

      return Try.success(new WorkspaceContext(targets.get()));
    }
  }
}
