package org.jetbrains.bsp.bazel.server.bazel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;

public class BazelRunner {

  private static final Logger LOGGER = LogManager.getLogger(BazelRunner.class);

  private static final String PUBLISH_ALL_ACTIONS = "--build_event_publish_all_actions";
  private static final String BES_BACKEND = "--bes_backend=grpc://localhost:";

  private final Semaphore processLock;
  private final String bazel;

  private Optional<Integer> besBackendPort = Optional.empty();

  public BazelRunner(String bazelBinaryPath) {
    this.processLock = new Semaphore(1, true);
    this.bazel = bazelBinaryPath;
  }

  public BazelRunnerCommandBuilder commandBuilder() {
    return new BazelRunnerCommandBuilder(this);
  }

  BazelProcessResult runBazelCommandBes(String command, List<String> flags, List<String> arguments) {

    List<String> newFlags = getBesFlags(flags);
    return runBazelCommand(command, newFlags, arguments);
  }

  private List<String> getBesFlags(List<String> flags) {
    List<String> newFlags = new ArrayList<>();
    newFlags.add(getBesBackendAddress());
    newFlags.add(PUBLISH_ALL_ACTIONS);
    newFlags.addAll(flags);

    return newFlags;
  }

  private String getBesBackendAddress() {
    Integer port = besBackendPort.orElseThrow(() -> new IllegalStateException("BES port not set"));

    return BES_BACKEND + port;
  }

  BazelProcessResult runBazelCommand(String command, List<String> flags, List<String> arguments) {
    if (arguments.isEmpty()) {
      LOGGER.fatal("Not enough arguments");
      throw new IllegalArgumentException("Not enough arguments");
    }

    try {
      return runBazelProcess(command, flags, arguments);
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private BazelProcessResult runBazelProcess(String command, List<String> flags, List<String> arguments)
      throws InterruptedException, IOException {
    List<String> processArgs = getProcessArgs(command, flags, arguments);
    LOGGER.info("Running: {}", processArgs);

    processLock.acquire();

    ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
    Process process = processBuilder.start();
    int exitCode = process.waitFor();

    processLock.release();

    return new BazelProcessResult(process.getInputStream(), process.getErrorStream(), exitCode);
  }

  private List<String> getProcessArgs(String command, List<String> flags, List<String> arguments) {
    List<String> processArgs = new ArrayList<>();

    processArgs.add(bazel);
    processArgs.add(command);
    processArgs.addAll(flags);
    processArgs.addAll(arguments);

    return processArgs;
  }

  public void setBesBackendPort(int port) {
    besBackendPort = Optional.of(port);
  }
}
