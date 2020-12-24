package org.jetbrains.bsp.bazel.server.bazel;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BazelRunner {

  private static final Logger LOGGER = LogManager.getLogger(BazelRunner.class);

  private static final String PUBLISH_ALL_ACTIONS = "--build_event_publish_all_actions";
  private static final String BES_BACKEND = "--bes_backend=grpc://localhost:";

  private final Semaphore processLock;
  private final String bazel;
  private Integer besBackendPort;

  public BazelRunner(String bazelBinaryPath) {
    this.processLock = new Semaphore(1, true);
    this.bazel = bazelBinaryPath;
  }

  public void setBesBackendPort(int port) {
    besBackendPort = port;
  }

  public ProcessResults runBazelCommandBes(String command, List<String> flags, List<String> arguments) {
    if (besBackendPort == null) {
      LOGGER.fatal("BES port not set");
      throw new IllegalStateException("BES port not set");
    }

    List<String> newFlags = getBesFlags(flags);
    return runBazelCommand(command, newFlags, arguments);
  }

  private List<String> getBesFlags(List<String> flags) {
    List<String> newFlags = new ArrayList<>();
    newFlags.add(BES_BACKEND + besBackendPort);
    newFlags.add(PUBLISH_ALL_ACTIONS);
    newFlags.addAll(flags);

    return newFlags;
  }

  public ProcessResults runBazelCommand(String command, List<String> flags, List<String> arguments) {
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

  private ProcessResults runBazelProcess(String command, List<String> flags, List<String> arguments)
      throws InterruptedException, IOException {
    List<String> processArgs = getProcessArgs(command, flags, arguments);
    LOGGER.info("Running: {}", processArgs);

    processLock.acquire();

    ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
    Process process = processBuilder.start();
    int exitCode = process.waitFor();

    processLock.release();

    return new ProcessResults(process.getInputStream(), process.getErrorStream(), exitCode);
  }

  private List<String> getProcessArgs(String command, List<String> flags, List<String> arguments) {
    List<String> processArgs = new ArrayList<>();

    processArgs.add(bazel);
    processArgs.add(command);
    processArgs.addAll(flags);
    processArgs.addAll(arguments);

    return processArgs;
  }
}
