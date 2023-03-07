/**
 * Copied from https://github.com/JetBrains/intellij-scala/blob/9395de0f3ae6e4c3f7411edabc5374e6595162f5/bsp/src/org/jetbrains/bsp/protocol/session/CancellableFuture.java
 */

package org.jetbrains.bsp.bazel.server.bsp;

import java.util.concurrent.CompletableFuture;

/**
 * JDK's CompletableFuture does not handle cancellation well.
 * When a `cancel` method is called on a derived future, created
 * with a transformation method like `thenApply`, the
 * cancellation is not propagated back to the original future.
 * Try this code to see the difference:
 * ```
 *     public static void main(String[] args) throws InterruptedException {
 *         var f1 = CancellableFuture.from(new CompletableFuture<>());
 *         var f2 = f1.thenApply(x ->x);
 *         f2.cancel(true);
 *         System.out.println(f1.isCancelled());
 *     }
 * ```
 * If you remove "CancellableFuture.from" call, you will get `false` instead of `true`
 */
public class CancellableFuture<T> extends CompletableFuture<T> {

    final private CompletableFuture<?> original;

    public CancellableFuture(CompletableFuture<?> original)
    {
        this.original = original;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        original.cancel(mayInterruptIfRunning);
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new CancellableFuture<>(original);
    }

    public static <U> CancellableFuture<U> from(CompletableFuture<U> original) {
        CancellableFuture<U> result = new CancellableFuture<>(original);
        original.whenComplete((value, error) -> {
            if(error != null) {
                result.completeExceptionally(error);
            } else {
                result.complete(value);
            }
        });
        return result;
    }
}