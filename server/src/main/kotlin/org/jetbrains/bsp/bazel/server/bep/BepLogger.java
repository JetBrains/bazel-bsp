package org.jetbrains.bsp.bazel.server.bep;

import ch.epfl.scala.bsp4j.BuildClient;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;

public class BepLogger extends BspClientLogger {

  private static final Logger LOGGER = LogManager.getLogger(BspClientLogger.class);

  private static final String ADDITIONAL_MESSAGE_PREFIX = "    ";

  private final BspClientLogger bspClientLogger;

  public BepLogger(BuildClient client, Optional<String> originId) {
    super();
    bspClientLogger =
        originId.map(oid -> new BspClientLogger().withOriginId(oid)).orElse(new BspClientLogger());
    bspClientLogger.initialize(client);
  }

  private BepLogger(BspClientLogger bspClientLogger) {
    super();
    this.bspClientLogger = bspClientLogger;
  }

  @Override
  public BepLogger withOriginId(String originId) {
    return new BepLogger(super.withOriginId(originId));
  }

  public void onProgress(BuildEventStreamProtos.Progress progress) {
    String output = progress.getStderr();
    if (!output.isBlank()) {
      logMessage(output);
      LOGGER.info(output);
    }
  }

  public void onBuildMetrics(BuildEventStreamProtos.BuildMetrics metrics) {
    // TODO: https://youtrack.jetbrains.com/issue/BAZEL-621
    Duration duration = Duration.ofMillis(metrics.getTimingMetrics().getWallTimeInMs());
    logMessage(String.format("Command completed in %s", Format.duration(duration)));
  }

  private void logMessage(String output) {
    bspClientLogger.message(output);
    String filteredOutput =
        Arrays.stream(output.split("\n"))
            .filter(element -> !element.startsWith(ADDITIONAL_MESSAGE_PREFIX))
            .collect(Collectors.joining("\n"));
    LOGGER.info(filteredOutput);
  }
}
