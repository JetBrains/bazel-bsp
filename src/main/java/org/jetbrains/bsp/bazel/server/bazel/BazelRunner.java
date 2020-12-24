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

  public ProcessResults runBazelCommandBes(String... args) {
    return runBazelCommandBes(Lists.newArrayList(args));
  }

  public ProcessResults runBazelCommandBes(List<String> args) {
    if (besBackendPort == null) {
      LOGGER.fatal("BES port not set");
      throw new IllegalStateException("BES port not set");
    }
    if (args.size() < 1) {
      LOGGER.fatal("Not enough arguments");
      throw new IllegalArgumentException("Not enough arguments");
    }

    List<String> newArgs = getNewArgs(args);
    return runBazelCommand(newArgs);
  }

  private List<String> getNewArgs(List<String> args) {
    List<String> newArgs = new ArrayList<>(args);
    newArgs.add(1, BES_BACKEND + besBackendPort);
    newArgs.add(2, PUBLISH_ALL_ACTIONS);

    return newArgs;
  }

  public ProcessResults runBazelCommand(String... args) {
    return runBazelCommand(Lists.newArrayList(args));
  }

  public ProcessResults runBazelCommand(List<String> args) {
    try {
      List<String> processArgs = getProcessArgs(args);
      LOGGER.info("Running: {}", processArgs);

      processLock.acquire();

      ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
      Process process = processBuilder.start();
      int exitCode = process.waitFor();

      processLock.release();

      return new ProcessResults(process.getInputStream(), process.getErrorStream(), exitCode);
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<String> getProcessArgs(List<String> args) {
    List<String> processArgs = new ArrayList<>();
    processArgs.add(bazel);
    processArgs.addAll(args);

    return processArgs;
  }
}
