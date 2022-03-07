package org.jetbrains.bsp.bazel.server.loggers;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.MessageType;
import ch.epfl.scala.bsp4j.StatusCode;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelProcessResult;

public class BuildClientLogger {

  private final BuildClient buildClient;

  public BuildClientLogger(BuildClient buildClient) {
    this.buildClient = buildClient;
  }

  public void logError(String errorMessage) {
    logIfNotBlank(MessageType.ERROR, errorMessage);
  }

  public void logMessage(String message) {
    logIfNotBlank(MessageType.LOG, message);
  }

  public void logBazelProcessResult(BazelProcessResult result) {
    String message = String.join("\n", result.getStderr());
    if (result.getStatusCode().equals(StatusCode.OK)) {
      logMessage(message);
    } else {
      logError(message);
    }
  }

  private void logIfNotBlank(MessageType messageType, String message) {
    if (!message.trim().isEmpty()) {
      log(messageType, message);
    }
  }

  private void log(MessageType messageType, String message) {
    LogMessageParams params = new LogMessageParams(messageType, message.trim());
    buildClient.onBuildLogMessage(params);
  }
}
