package org.jetbrains.bsp.bazel.bazelrunner;

import ch.epfl.scala.bsp4j.StatusCode;
import com.google.common.collect.Iterables;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelProcessResult;

public class BazelDataResolver {

  private static final String EXECUTION_ROOT_PARAMETER = "execution_root";
  private static final String WORKPLACE_ROOT_PARAMETER = "workspace";
  private static final String BAZEL_BIN_ROOT_PARAMETER = "bazel-bin";
  private static final String BAZEL_VERSION_PARAMETER = "release";

  private final BazelRunner bazelRunner;

  public BazelDataResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public BazelData resolveBazelData() throws BazelDataResolverException{
    String execRoot = readOnlyBazelLine(EXECUTION_ROOT_PARAMETER);
    String workspaceRoot = readOnlyBazelLine(WORKPLACE_ROOT_PARAMETER);
    String binRoot = readOnlyBazelLine(BAZEL_BIN_ROOT_PARAMETER);
    String version = readOnlyBazelLine(BAZEL_VERSION_PARAMETER);
    Path workspacePath = Paths.get(execRoot);
    String workspaceLabel = workspacePath.toFile().getName();
    Path bspProjectRoot = Paths.get("").toAbsolutePath().normalize();
    return new BazelData(execRoot, workspaceRoot, binRoot, workspaceLabel, version, bspProjectRoot);
  }

  private String readOnlyBazelLine(String argument) throws BazelDataResolverException {
    BazelProcessResult bazelProcessResult =
        bazelRunner
            .commandBuilder()
            .info()
            .withArgument(argument)
            .executeBazelCommand()
            .waitAndGetResult();
    List<String> output = bazelProcessResult.getStdout();

    if (!bazelProcessResult.getStatusCode().equals(StatusCode.OK)) {
      throw new BazelDataResolverException("Failed to read bazel info: " + argument, bazelProcessResult);
    }

    if (output.size() != 1) {
      throw new BazelDataResolverException("Bazel info should return exactly one line: " + argument, bazelProcessResult);
    }

    return Iterables.getOnlyElement(output);
  }

  public static class BazelDataResolverException extends Exception {
    private final BazelProcessResult bazelProcessResult;

    public BazelDataResolverException(String message, BazelProcessResult bazelProcessResult) {
      super(message);
      this.bazelProcessResult = bazelProcessResult;
    }

    public BazelProcessResult getBazelProcessResult() {
      return bazelProcessResult;
    }
  }
}
