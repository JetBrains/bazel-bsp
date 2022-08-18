package org.jetbrains.bsp.bazel.logger;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.MessageType;
import java.time.Duration;
import java.util.function.Supplier;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.commons.Stopwatch;

public class BspClientLogger {

  private static final Duration LOG_OPERATION_THRESHOLD = Duration.ofMillis(100);
  private BuildClient bspClient;

  public void error(String errorMessage, String originId) {
    log(MessageType.ERROR, errorMessage, originId);
  }

  public void message(String format, Object... args) {
    log(MessageType.LOG, String.format(format, args), null);
  }

  public void message(String message, String originId) {
    log(MessageType.LOG, message, originId);
  }

  public void warn(String format, Object... args) {
    warn(String.format(format, args));
  }

  public void warn(String message) {
    log(MessageType.WARNING, message, null);
  }

  private void log(MessageType messageType, String message, String originId) {
    if (bspClient == null) return;

    if (!message.trim().isEmpty()) {
      var params = new LogMessageParams(messageType, message);
      params.setOriginId(originId);
      bspClient.onBuildLogMessage(params);
    }
  }

  public <T> T timed(String description, String originId, Supplier<T> supplier) {
    var sw = Stopwatch.start();
    T result = supplier.get();
    var duration = sw.stop();
    if (duration.compareTo(LOG_OPERATION_THRESHOLD) >= 0) {
      message(
          String.format("%s completed in %s", description, Format.duration(duration)), originId);
    }
    return result;
  }

  public void timed(String description, String originId, Runnable runnable) {
    timed(
        description,
        originId,
        () -> {
          runnable.run();
          return null;
        });
  }

  public void initialize(BuildClient buildClient) {
    this.bspClient = buildClient;
  }
}
