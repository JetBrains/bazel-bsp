package org.jetbrains.bsp.bazel.server.resolver;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.jetbrains.bsp.bazel.server.data.ProcessResults;

public class BazelRunner {

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
      throw new IllegalStateException("BES port not set");
    }
    if (args.size() < 1) {
      throw new IllegalArgumentException("Not enough arguments");
    }
    List<String> newArgs = new ArrayList<>(args);
    newArgs.add(1, BES_BACKEND + besBackendPort);
    newArgs.add(2, PUBLISH_ALL_ACTIONS);
    return runBazelCommand(newArgs);
  }

  public ProcessResults runBazelCommand(String... args) {
    return runBazelCommand(Lists.newArrayList(args));
  }

  public ProcessResults runBazelCommand(List<String> args) {
    try {
      List<String> processArgs = new ArrayList<>();
      processArgs.add(bazel);
      processArgs.addAll(args);
      System.out.printf("Running: %s%n", processArgs); // TODO better logging
      processLock.acquire();
      ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
      Process process = processBuilder.start();

      InputStream os = process.getInputStream();
      InputStream es = process.getErrorStream();
      int exitCode = process.waitFor();
      processLock.release();

      return new ProcessResults(process.getInputStream(), process.getErrorStream(), exitCode);
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
