package org.jetbrains.bsp.bazel;

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
    buildClient.onBuildLogMessage(params);
    throw new RuntimeException(errorMessage);
  }

  public void logMessage(String message) {
    LogMessageParams params = new LogMessageParams(MessageType.LOG, message);
    buildClient.onBuildLogMessage(params);
  }
}
