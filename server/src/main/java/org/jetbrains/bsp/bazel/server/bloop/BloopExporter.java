package org.jetbrains.bsp.bazel.server.bloop;

import ch.epfl.scala.bsp4j.BuildClient;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.DidChangeBuildTarget;
import ch.epfl.scala.bsp4j.LogMessageParams;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.ShowMessageParams;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import ch.epfl.scala.bsp4j.TaskProgressParams;
import ch.epfl.scala.bsp4j.TaskStartParams;
import com.google.common.collect.Sets;
import io.grpc.ServerBuilder;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.server.common.ServerContainer;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider;

class BloopExporter {

  private final BspInfo bspInfo;
  private final WorkspaceContextProvider workspaceContextProvider;
  private final Path workspaceRoot;

  public BloopExporter(
      BspInfo bspInfo, Path workspaceRoot, WorkspaceContextProvider workspaceContextProvider) {
    this.bspInfo = bspInfo;
    this.workspaceContextProvider = workspaceContextProvider;
    this.workspaceRoot = workspaceRoot;
  }

  public void export() throws BazelExportFailedException {
    var serverContainer =
        ServerContainer.create(
            bspInfo,
            workspaceContextProvider,
            Option.of(this.workspaceRoot),
            Option.of(new NoopProjectStorage()));
    var projectProvider = serverContainer.projectProvider;
    var client = new BloopBuildClient(System.out);
    initializeClient(serverContainer, client);

    var project = projectProvider.refreshAndGet();

    var failedTargets = client.getFailedTargets();
    var failedTransitiveTargets =
        failedTargets.removeAll(
            project.modules().map(m -> new BuildTargetIdentifier(m.label().getValue())));

    if (failedTransitiveTargets.nonEmpty()) {
      throw new BazelExportFailedException(failedTransitiveTargets);
    }

    serverContainer.bspClientLogger.timed(
        "Exporting to bloop",
        () -> {
          var bloopPath = bspInfo.bspProjectRoot().resolve(".bloop");
          var writtenFiles = new BspProjectExporter(project, bloopPath).export();
          cleanUpBloopDirectory(writtenFiles, bloopPath);
        });
  }

  private void initializeClient(ServerContainer serverContainer, BloopBuildClient client) {
    serverContainer.bspClientLogger.initialize(client);
    var bepServer = new BepServer(client, new DiagnosticsService(serverContainer.bazelInfo));
    serverContainer.compilationManager.setBepServer(bepServer);

    var grpcServer = ServerBuilder.forPort(0).addService(bepServer).build();
    try {
      grpcServer.start();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    serverContainer.bazelRunner.setBesBackendPort(grpcServer.getPort());
  }

  private void cleanUpBloopDirectory(Set<Path> expected, Path bloopRoot) {
    try (var listResult = Files.list(bloopRoot)) {
      var existingFiles =
          listResult
              .filter(name -> name.toString().endsWith(".config.json"))
              .collect(HashSet.collector());

      var extraFiles = existingFiles.diff(expected);
      extraFiles.forEach(p -> p.toFile().delete());
    } catch (Exception e) {
      // it's fine
    }
  }

  public static class BazelExportFailedException extends RuntimeException {
    private final Set<BuildTargetIdentifier> failedTargets;

    public BazelExportFailedException(Set<BuildTargetIdentifier> failedTargets) {
      this.failedTargets = failedTargets;
    }

    public Set<BuildTargetIdentifier> getFailedTargets() {
      return this.failedTargets;
    }
  }

  private static class BloopBuildClient implements BuildClient {

    private final PrintStream out;
    private final java.util.Set<BuildTargetIdentifier> failedTargets = Sets.newHashSet();

    BloopBuildClient(PrintStream out) {
      this.out = out;
    }

    public Set<BuildTargetIdentifier> getFailedTargets() {
      return HashSet.ofAll(failedTargets);
    }

    @Override
    public void onBuildShowMessage(ShowMessageParams showMessageParams) {}

    @Override
    public void onBuildLogMessage(LogMessageParams logMessageParams) {
      out.println(logMessageParams.getMessage());
    }

    @Override
    public void onBuildTaskStart(TaskStartParams taskStartParams) {}

    @Override
    public void onBuildTaskProgress(TaskProgressParams taskProgressParams) {}

    @Override
    public void onBuildTaskFinish(TaskFinishParams taskFinishParams) {}

    @Override
    public void onBuildPublishDiagnostics(PublishDiagnosticsParams publishDiagnosticsParams) {
      if (publishDiagnosticsParams.getDiagnostics().stream()
          .anyMatch(d -> d.getSeverity() == DiagnosticSeverity.ERROR)) {
        this.failedTargets.add(publishDiagnosticsParams.getBuildTarget());
      }
    }

    @Override
    public void onBuildTargetDidChange(DidChangeBuildTarget didChangeBuildTarget) {}
  }

  private static final class NoopProjectStorage implements ProjectStorage {

    @Override
    public Option<Project> load() {
      return Option.none();
    }

    @Override
    public void store(Project project) {}
  }
}
