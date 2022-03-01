package org.jetbrains.bsp.bazel.bazelrunner;

import ch.epfl.scala.bsp4j.StatusCode;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.server.loggers.BuildClientLogger;

public class BazelRunner {

  private static final Logger LOGGER = LogManager.getLogger(BazelRunner.class);

  private static final String PUBLISH_ALL_ACTIONS = "--build_event_publish_all_actions";
  private static final String BES_BACKEND = "--bes_backend=grpc://localhost:";

  private final String bazel;

  private Optional<Integer> besBackendPort = Optional.empty();
  private Optional<BuildClientLogger> buildClientLogger = Optional.empty();
  private final Supplier<File> workspaceRoot;

  public BazelRunner(String bazelBinaryPath) {
    this.bazel = bazelBinaryPath;
    this.workspaceRoot = Suppliers.memoize(this::resolveWorkspaceRoot);
  }

  private File resolveWorkspaceRoot() {
    var builder = new ProcessBuilder(bazel, "info", "workspace");
    try {
      var process = new BazelProcess(builder.start());
      var result = process.waitAndGetResult();
      if (result.getStatusCode() != StatusCode.OK) {
        throw new RuntimeException(
            "Failed to run bazel info workspace. Make sure that the project is created inside a"
                + " bazel workspace");
      }
      return new File(result.getStdout().get(0));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public BazelRunnerCommandBuilder commandBuilder() {
    return new BazelRunnerCommandBuilder(this);
  }

  public BazelProcess runBazelCommandBes(String command, List<String> flags, List<String> arguments) {
    List<String> newFlags = getBesFlags(flags);
    return runBazelCommand(command, newFlags, arguments);
  }

  private List<String> getBesFlags(List<String> flags) {
    List<String> newFlags = Lists.newArrayList(getBesBackendAddress(), PUBLISH_ALL_ACTIONS);
    newFlags.addAll(flags);

    return newFlags;
  }

  private String getBesBackendAddress() {
    Integer port = besBackendPort.orElseThrow(() -> new IllegalStateException("BES port not set"));

    return BES_BACKEND + port;
  }

  public BazelProcess runBazelCommand(String command, List<String> flags, List<String> arguments) {
    try {
      LOGGER.info(
          "Waiting for bazel - command: {}, flags: {}, arguments: {}", command, flags, arguments);
      return runBazelProcess(command, flags, arguments);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private synchronized BazelProcess runBazelProcess(
      String command, List<String> flags, List<String> arguments) throws IOException {
    List<String> processArgs = getProcessArgs(command, flags, arguments);
    LOGGER.info("Running: {}", processArgs);

    ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
    processBuilder.directory(workspaceRoot.get());
    Process process = processBuilder.start();

    return new BazelProcess(process);
  }

  private List<String> getProcessArgs(String command, List<String> flags, List<String> arguments) {
    List<String> processArgs = Lists.newArrayList(bazel, command);
    processArgs.addAll(flags);
    processArgs.addAll(arguments);

    return processArgs;
  }

  public void setBesBackendPort(int port) {
    besBackendPort = Optional.of(port);
  }

  public void setLogger(BuildClientLogger buildClientLogger) {
    this.buildClientLogger = Optional.ofNullable(buildClientLogger);
  }
}
