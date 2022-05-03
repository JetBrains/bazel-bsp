package org.jetbrains.bsp.bazel.executioncontext.api.entries;

/**
 * Base <code>ExecutionContext</code> entity class - you need to extend it or <code>
 * ExecutionContextListEntity</code> or <code>ExecutionContextSingletonEntity</code> if you want to
 * create your entity.
 *
 * @see org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextListEntity
 * @see org.jetbrains.bsp.bazel.executioncontext.api.entries.ExecutionContextSingletonEntity
 */
public abstract class ExecutionContextEntity {}
