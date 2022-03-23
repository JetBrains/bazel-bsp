package org.jetbrains.bsp.bazel.logger;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.MessageType;

public class BuildClientLogger {

  private BuildClient buildClient;

  public void logError(String errorMessage) {
    log(MessageType.ERROR, errorMessage);
  }

  public void logMessage(String message) {
    log(MessageType.LOG, message);
  }

  private void log(MessageType messageType, String message) {
    if (buildClient == null) return;

    if (!message.trim().isEmpty()) {
      var params = new LogMessageParams(messageType, message);
      buildClient.onBuildLogMessage(params);
    }
  }

  public void setBuildClient(BuildClient buildClient) {
    this.buildClient = buildClient;
  }
}
