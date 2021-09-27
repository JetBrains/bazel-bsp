package org.jetbrains.bsp.bazel.server.bsp;

import ch.epfl.scala.bsp4j.BuildClient;
import io.grpc.Server;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import org.eclipse.lsp4j.jsonrpc.Launcher;

public class BspIntegrationData {

  private final PrintStream stdout;
  private final InputStream stdin;
  private final ExecutorService executor;
  private final PrintWriter traceWriter;

  private Launcher<BuildClient> launcher;
  private Server server;

  public BspIntegrationData(
      PrintStream stdout, InputStream stdin, ExecutorService executor, PrintWriter traceWriter) {
    this.stdout = stdout;
    this.stdin = stdin;
    this.executor = executor;
    this.traceWriter = traceWriter;
  }

  public PrintStream getStdout() {
    return stdout;
  }

  public InputStream getStdin() {
    return stdin;
  }

  public ExecutorService getExecutor() {
    return executor;
  }

  public PrintWriter getTraceWriter() {
    return traceWriter;
  }

  public Launcher<BuildClient> getLauncher() {
    return launcher;
  }

  public void setLauncher(Launcher<BuildClient> launcher) {
    this.launcher = launcher;
  }

  public Server getServer() {
    return server;
  }

  public void setServer(Server server) {
    this.server = server;
  }
}
