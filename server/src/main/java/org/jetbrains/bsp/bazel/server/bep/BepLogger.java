package org.jetbrains.bsp.bazel.server.bep;

import ch.epfl.scala.bsp4j.BuildClient;
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.Format;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BepLogger extends BspClientLogger {

    private static final Logger LOGGER = LogManager.getLogger(BspClientLogger.class);

    private static final String ADDITIONAL_MESSAGE_PREFIX = "    ";

    private final BspClientLogger bspClientLogger;

    public BepLogger(BuildClient client, String originId) {
        super();
        bspClientLogger = new BspClientLogger().withOriginId(originId);
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
        // TODO: We potentially might want to obtain more metrics data than just running time
        Duration duration = Duration.ofMillis(metrics.getTimingMetrics().getWallTimeInMs());
        logMessage(String.format("Command completed in %s", Format.duration(duration)));
    }

    private void logMessage(String output) {
        bspClientLogger.message(output);
        String filteredOutput = Arrays.stream(output.split("\n"))
                .filter(element -> !element.startsWith(ADDITIONAL_MESSAGE_PREFIX))
                .collect(Collectors.joining("\n"));
        LOGGER.info(filteredOutput);
    }
}
