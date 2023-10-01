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
  private String originId;

  public BspClientLogger withOriginId(String originId) {
    BspClientLogger bspClientLogger = new BspClientLogger();
    bspClientLogger.originId = originId;
    bspClientLogger.bspClient = bspClient;
    return bspClientLogger;
  }

  public void error(String errorMessage) {
    log(MessageType.ERROR, errorMessage);
  }

  public void message(String format, Object... args) {
    message(String.format(format, args));
  }

  public void message(String message) {
    log(MessageType.LOG, message);
  }

  public void warn(String format, Object... args) {
    warn(String.format(format, args));
  }

  public void warn(String message) {
    log(MessageType.WARNING, message);
  }

  private void log(MessageType messageType, String message) {
    if (bspClient == null) return;

    if (!message.trim().isEmpty()) {
      var params = new LogMessageParams(messageType, message);
      params.setOriginId(originId);
      bspClient.onBuildLogMessage(params);
    }
  }

  public <T> T timed(String description, Supplier<T> supplier) {
    var sw = Stopwatch.start();
    T result = supplier.get();
    var duration = sw.stop();
    logDuration(description, duration);
    return result;
  }

  public void logDuration(String description, Duration duration) {
    if (duration.compareTo(LOG_OPERATION_THRESHOLD) >= 0) {
      message("Task '%s' completed in %s", description, Format.duration(duration));
    }
  }

  public void timed(String description, Runnable runnable) {
    timed(
        description,
        () -> {
          runnable.run();
          return null;
        });
  }

  public void initialize(BuildClient buildClient) {
    this.bspClient = buildClient;
  }
}
