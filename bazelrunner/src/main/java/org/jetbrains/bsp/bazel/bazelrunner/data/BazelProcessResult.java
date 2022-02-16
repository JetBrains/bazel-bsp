package org.jetbrains.bsp.bazel.bazelrunner.data;

import ch.epfl.scala.bsp4j.StatusCode;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelStreamReader;
import org.jetbrains.bsp.bazel.commons.ExitCodeMapper;
import org.jetbrains.bsp.bazel.commons.Lazy;

public class BazelProcessResult {

  private static final String LINES_DELIMITER = "\n";

  private final StreamStorage stdout;
  private final StreamStorage stderr;
  private final int exitCode;

  public BazelProcessResult(InputStream stdout, InputStream stderr, int exitCode) {
    this.stdout = new StreamStorage(stdout);
    this.stderr = new StreamStorage(stderr);
    this.exitCode = exitCode;
  }

  public StatusCode getStatusCode() {
    return ExitCodeMapper.mapExitCode(exitCode);
  }

  public List<String> getStdout() {
    return stdout.getValue().get();
  }

  public String getJoinedStderr() {
    List<String> lines = stderr.getValue().get();
    return String.join(LINES_DELIMITER, lines);
  }

  private static class StreamStorage extends Lazy<List<String>> {

    // TODO make this a weak reference?
    private final InputStream input;

    public StreamStorage(InputStream input) {
      this.input = input;
    }

    private Optional<List<String>> supply() {
      return Optional.of(BazelStreamReader.drainStream(input));
    }

    @Override
    protected Supplier<Optional<List<String>>> calculateValue() {
      return this::supply;
    }

    @Override
    public void recalculateValue() {
      // Do nothing. The original input stream is probably drained at this point.
    }
  }
}
