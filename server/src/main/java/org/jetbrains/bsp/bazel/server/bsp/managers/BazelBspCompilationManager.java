package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcess;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.data.BazelData;
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bep.BepServer;

public class BazelBspCompilationManager {

  private BepServer bepServer;
  private final BazelRunner bazelRunner;
  private final BazelData bazelData;

  public BazelBspCompilationManager(BazelRunner bazelRunner, BazelData bazelData) {
    this.bazelRunner = bazelRunner;
    this.bazelData = bazelData;
  }

  public BepBuildResult buildTargetsWithBep(
      List<BuildTargetIdentifier> includedTargets,
      List<BuildTargetIdentifier> excludedTargets,
      List<String> extraFlags) {
    final Map<String, String> diagnosticsProtosLocations =
        bepServer.getDiagnosticsProtosLocations();
    BazelProcess bazelProcess =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withTargets(includedTargets, excludedTargets)
            .executeBazelBesCommand();

    Build.QueryResult queryResult = getQueryResultForProcess(bazelProcess);

    cacheProtoLocations(diagnosticsProtosLocations, queryResult);

    var result =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlags(extraFlags)
            .withTargets(includedTargets, excludedTargets)
            .executeBazelBesCommand()
            .waitAndGetResult();

    emitDiagnosticsFromCache(diagnosticsProtosLocations);

    return new BepBuildResult(result, bepServer.getBepOutput());
  }

  private void emitDiagnosticsFromCache(Map<String, String> diagnosticsProtosLocations) {
    for (Map.Entry<String, String> diagnostics : diagnosticsProtosLocations.entrySet()) {
      String target = diagnostics.getKey();
      String diagnosticsPath = diagnostics.getValue();
      BuildTargetIdentifier targetIdentifier = new BuildTargetIdentifier(target);
      // TODO (abrams27) is it ok?
      bepServer.emitDiagnostics(
          bepServer.collectDiagnostics(targetIdentifier, diagnosticsPath), targetIdentifier);
    }
  }

  private void cacheProtoLocations(
      Map<String, String> diagnosticsProtosLocations, Build.QueryResult queryResult) {
    queryResult.getTargetList().stream()
        .map(Build.Target::getRule)
        .filter(this::isWorkspacePackage)
        .forEach(
            rule ->
                rule.getRuleOutputList().stream()
                    .filter(output -> output.contains(Constants.DIAGNOSTICS))
                    .forEach(output -> cacheProtos(diagnosticsProtosLocations, rule, output)));
  }

  private void cacheProtos(
      Map<String, String> diagnosticsProtosLocations, Build.Rule rule, String output) {
    diagnosticsProtosLocations.put(
        rule.getName(), convertOutputToPath(output, bazelData.getBinRoot()));
  }

  private boolean isWorkspacePackage(Build.Rule rule) {
    return !rule.getName().startsWith("@");
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
  }

  private Build.QueryResult getQueryResultForProcess(BazelProcess process) {
    try {
      return Build.QueryResult.parseFrom(process.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String convertOutputToPath(String output, String prefix) {
    String pathToFile = output.replaceAll("(//|:)", "/");
    return prefix + pathToFile;
  }
}
