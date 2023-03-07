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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.logger.BspClientLogger;
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo;
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager;
import org.jetbrains.bsp.bazel.server.common.ServerContainer;
import org.jetbrains.bsp.bazel.server.diagnostics.DiagnosticsService;
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider;

class BloopExporter {

  private final BspInfo bspInfo;
  private final WorkspaceContextProvider workspaceContextProvider;
  private final Path workspaceRoot;

  static void validateNoFailedExternalTargets(
      Set<BuildTargetIdentifier> projectTargets, Set<BuildTargetIdentifier> failedTargets)
      throws BazelExportFailedException {
    var failedExternalTargets =
        failedTargets.stream()
            .filter(Predicate.not(projectTargets::contains))
            .collect(Collectors.toSet());

    if (!failedExternalTargets.isEmpty()) {
      throw new BazelExportFailedException(failedExternalTargets);
    }
  }

  public BloopExporter(
      BspInfo bspInfo, Path workspaceRoot, WorkspaceContextProvider workspaceContextProvider) {
    this.bspInfo = bspInfo;
    this.workspaceContextProvider = workspaceContextProvider;
    this.workspaceRoot = workspaceRoot;
  }

  public void export(CancelChecker cancelChecker) throws BazelExportFailedException {
    var bspClientLogger = new BspClientLogger();
    var bspClientTestNotifier = new BspClientTestNotifier();
    var bazelRunner = BazelRunner.of(workspaceContextProvider, bspClientLogger, workspaceRoot);
    var compilationManager = new BazelBspCompilationManager(bazelRunner);
    var serverContainer =
        ServerContainer.create(
            bspInfo,
            workspaceContextProvider,
            new NoopProjectStorage(),
            bspClientLogger,
            bspClientTestNotifier,
            bazelRunner,
            compilationManager);
    var projectProvider = serverContainer.getProjectProvider();
    var client = new BloopBuildClient(System.out);
    initializeClient(serverContainer, client);

    var project = projectProvider.refreshAndGet(cancelChecker);
    var projectTargets =
        project.getModules().stream()
            .map(m -> new BuildTargetIdentifier(m.getLabel().getValue()))
            .collect(Collectors.toSet());

    validateNoFailedExternalTargets(projectTargets, client.getFailedTargets());

    serverContainer
        .getBspClientLogger()
        .timed(
            "Exporting to bloop",
            () -> {
              var bloopPath = bspInfo.bspProjectRoot().resolve(".bloop");
              var writtenFiles = new BspProjectExporter(project, bloopPath).export();
              cleanUpBloopDirectory(writtenFiles, bloopPath);
            });
  }

  private void initializeClient(ServerContainer serverContainer, BloopBuildClient client) {
    serverContainer.getBspClientLogger().initialize(client);
    var bepServer =
        new BepServer(
            client, new DiagnosticsService(serverContainer.getBazelInfo().getWorkspaceRoot()));
    serverContainer.getCompilationManager().setBepServer(bepServer);

    var grpcServer = ServerBuilder.forPort(0).addService(bepServer).build();
    try {
      grpcServer.start();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    serverContainer.getBazelRunner().setBesBackendPort(grpcServer.getPort());
  }

  private void cleanUpBloopDirectory(Set<Path> expected, Path bloopRoot) {
    try {
      Files.list(bloopRoot)
          .filter(name -> name.toString().endsWith(".config.json"))
          .filter(p -> !expected.contains(p))
          .forEach(p -> p.toFile().delete());
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
      return new HashSet<>(failedTargets);
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
    public Project load() {
      return null;
    }

    @Override
    public void store(Project project) {}
  }
}
