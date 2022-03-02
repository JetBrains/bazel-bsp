package org.jetbrains.bsp.bazel.workspacecontext;

import io.vavr.control.Try;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.executioncontext.api.ExecutionContext;
import org.jetbrains.bsp.bazel.workspacecontext.entries.ExecutionContextTargetsEntity;

public class WorkspaceContext extends ExecutionContext {

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

    private Optional<ExecutionContextTargetsEntity> targets = Optional.empty();

    private Builder() {}

    public Builder targets(ExecutionContextTargetsEntity targets) {
      this.targets = Optional.ofNullable(targets);

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
