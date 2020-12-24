package org.jetbrains.bsp.bazel.server.bazel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import ch.epfl.scala.bsp4j.StatusCode;
import org.jetbrains.bsp.bazel.server.bazel.utils.ExitCodeMapper;

public class ProcessResults {

  private final InputStream stdout;
  private final InputStream stderr;
  private final int exitCode;

  public ProcessResults(InputStream stdout, InputStream stderr, int exitCode) {
    this.stdout = stdout;
    this.stderr = stderr;
    this.exitCode = exitCode;
  }

  public InputStream getStdoutStream() {
    return stdout;
  }

  public InputStream getStderrStream() {
    return stderr;
  }

  public StatusCode getExitCode() {
    return ExitCodeMapper.mapExitCode(exitCode);
  }

  public List<String> getStdout() {
    return drainStream(stdout);
  }

  public List<String> getStderr() {
    return drainStream(stderr);
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
