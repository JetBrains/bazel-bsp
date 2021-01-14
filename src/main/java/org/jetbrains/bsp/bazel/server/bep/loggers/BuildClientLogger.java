package org.jetbrains.bsp.bazel.server.bep.loggers;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.MessageType;

public class BuildClientLogger {

  private final BuildClient buildClient;

  public BuildClientLogger(BuildClient buildClient) {
    this.buildClient = buildClient;
  }

  public void logError(String errorMessage) {
    LogMessageParams params = new LogMessageParams(MessageType.ERROR, errorMessage);
    log(params);
  }

  public void logMessage(String message) {
    LogMessageParams params = new LogMessageParams(MessageType.LOG, message);
    log(params);
  }

  private void log(LogMessageParams params) {
    buildClient.onBuildLogMessage(params);
  }
}
