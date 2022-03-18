package org.jetbrains.bsp.bazel.bazelrunner.outputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncOutputProcessor {

  private final ExecutorService executorService;
  private final List<Future<?>> runningProcessors;

  public AsyncOutputProcessor() {
    executorService = Executors.newCachedThreadPool();
    runningProcessors = new ArrayList<>();
  }

  public void start(InputStream inputStream, OutputHandler... handlers) {
    Runnable runnable =
        () -> {
          try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while (!Thread.currentThread().isInterrupted()) {
              var line = reader.readLine();
              if (line == null) return;
              Arrays.stream(handlers).forEach(h -> h.onNextLine(line));
            }
          } catch (IOException e) {
            if (Thread.currentThread().isInterrupted()) {
              return;
            }
            throw new RuntimeException(e);
          }
        };

    var future = executorService.submit(runnable);
    runningProcessors.add(future);
  }

  public void shutdown() {
    runningProcessors.forEach(
        p -> {
          try {
            p.get();
          } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
          }
        });
    executorService.shutdown();
  }
}
