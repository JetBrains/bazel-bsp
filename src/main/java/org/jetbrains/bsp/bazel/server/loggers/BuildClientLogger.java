package org.jetbrains.bsp.bazel.server.loggers;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.MessageType;

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
