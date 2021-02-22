package org.jetbrains.bsp.bazel.server.bazel.data;

import ch.epfl.scala.bsp4j.StatusCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.utils.ExitCodeMapper;

public class BazelProcessResult {

  private static final String LINES_DELIMITER = "\n";

  private final InputStream stdout;
  private final List<String> stderr;
  private final int exitCode;

  public BazelProcessResult(InputStream stdout, InputStream stderr, int exitCode) {
    this.stdout = stdout;
    this.stderr = drainStream(stderr);
    this.exitCode = exitCode;
  }

  public InputStream getStdoutStream() {
    return stdout;
  }

  public StatusCode getStatusCode() {
    return ExitCodeMapper.mapExitCode(exitCode);
  }

  public List<String> getStdout() {
    return drainStream(stdout);
  }

  public String getJoinedStderr() {
    List<String> lines = getStderr();
    return String.join(LINES_DELIMITER, lines);
  }

  public List<String> getStderr() {
    return stderr;
  }

  private List<String> drainStream(InputStream stream) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      List<String> list = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        list.add(line.trim());
      }
      return list;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
