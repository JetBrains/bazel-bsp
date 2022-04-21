package org.jetbrains.bsp.bazel.bazelrunner;

import com.google.common.collect.Lists;
import io.vavr.control.Option;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;

public class BazelRunner {

  private static final Logger LOGGER = LogManager.getLogger(BazelRunner.class);

  private static final String PUBLISH_ALL_ACTIONS = "--build_event_publish_all_actions";
  private static final String BES_BACKEND = "--bes_backend=grpc://localhost:";

  private final BazelPathProvider bazelPathProvider;

  private Option<Integer> besBackendPort = Option.none();
  private final BspClientLogger bspClientLogger;
  private final BazelInfo bazelInfo;

  private final List<String> defaultFlags;

  // This is runner without workspace path. It is used to determine workspace
  // path and create a fully functional runner.
  public static BazelRunner inCwd(BazelPathProvider bazelPath, BspClientLogger bspClientLogger) {
    return new BazelRunner(bazelPath, bspClientLogger, null, List.of());
  }

  public static BazelRunner of(
      BazelPathProvider bazelPath,
      BspClientLogger bspClientLogger,
      BazelInfo bazelInfo,
      List<String> defaultFlags) {
    return new BazelRunner(bazelPath, bspClientLogger, bazelInfo, defaultFlags);
  }

  private BazelRunner(
      BazelPathProvider bazelPathProvider,
      BspClientLogger bspClientLogger,
      BazelInfo bazelInfo,
      List<String> defaultFlags) {
    this.bazelPathProvider = bazelPathProvider;
    this.bspClientLogger = bspClientLogger;
    this.bazelInfo = bazelInfo;
    this.defaultFlags = defaultFlags;
  }

  public BazelRunnerCommandBuilder commandBuilder() {
    return new BazelRunnerCommandBuilder(this);
  }

  BazelProcess runBazelCommandBes(String command, List<String> flags, List<String> arguments) {
    List<String> newFlags = getBesFlags(flags);
    return runBazelCommand(command, newFlags, arguments);
  }

  private List<String> getBesFlags(List<String> flags) {
    List<String> newFlags = Lists.newArrayList(getBesBackendAddress(), PUBLISH_ALL_ACTIONS);
    newFlags.addAll(flags);

    return newFlags;
  }

  private String getBesBackendAddress() {
    var port = besBackendPort.getOrElseThrow(() -> new IllegalStateException("BES port not set"));
    return BES_BACKEND + port;
  }

  BazelProcess runBazelCommand(String command, List<String> flags, List<String> arguments) {
    try {
      return runBazelProcess(command, flags, arguments);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private synchronized BazelProcess runBazelProcess(
      String command, List<String> flags, List<String> arguments) throws IOException {
    var processArgs = getProcessArgs(command, flags, arguments);

    var message = "Invoking: " + String.join(" ", processArgs);
    LOGGER.info(message);
    bspClientLogger.message(message);

    var processBuilder = new ProcessBuilder(processArgs);
    if (bazelInfo != null) {
      processBuilder.directory(bazelInfo.workspaceRoot().toFile());
    }
    var process = processBuilder.start();

    return new BazelProcess(process, bspClientLogger);
  }

  private List<String> getProcessArgs(String command, List<String> flags, List<String> arguments) {
    var processArgs = Lists.newArrayList(bazelPathProvider.currentBazelPath(), command);
    processArgs.addAll(defaultFlags);
    processArgs.addAll(flags);
    processArgs.addAll(arguments);
    return processArgs;
  }

  public void setBesBackendPort(int port) {
    besBackendPort = Option.of(port);
  }
}
