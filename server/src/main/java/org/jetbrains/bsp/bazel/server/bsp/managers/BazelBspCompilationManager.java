package org.jetbrains.bsp.bazel.server.bsp.managers;

import static io.vavr.API.unchecked;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import io.vavr.collection.List;
import java.util.Map;
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo;
import org.jetbrains.bsp.bazel.bazelrunner.BazelProcess;
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner;
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.projectview.model.TargetSpecs;
import org.jetbrains.bsp.bazel.server.bep.BepServer;

public class BazelBspCompilationManager {

  private BepServer bepServer;
  private final BazelRunner bazelRunner;
  private final BazelInfo bazelInfo;

  public BazelBspCompilationManager(BazelRunner bazelRunner, BazelInfo bazelInfo) {
    this.bazelRunner = bazelRunner;
    this.bazelInfo = bazelInfo;
  }

  public BepBuildResult buildTargetsWithBep(TargetSpecs targetSpecs) {
    return buildTargetsWithBep(targetSpecs, List.empty());
  }

  public BepBuildResult buildTargetsWithBep(TargetSpecs targetSpecs, List<String> extraFlags) {
    var diagnosticsProtosLocations = bepServer.getDiagnosticsProtosLocations();
    var bazelProcess =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelFlag.outputProto())
            .withTargets(targetSpecs.included().asJava(), targetSpecs.excluded().asJava())
            .executeBazelBesCommand();

    var queryResult = getQueryResultForProcess(bazelProcess);

    cacheProtoLocations(diagnosticsProtosLocations, queryResult);

    var result =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlags(extraFlags.asJava())
            .withTargets(targetSpecs.included().asJava(), targetSpecs.excluded().asJava())
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
        rule.getName(), convertOutputToPath(output, bazelInfo.binRoot()));
  }

  private boolean isWorkspacePackage(Build.Rule rule) {
    return !rule.getName().startsWith("@");
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
  }

  private Build.QueryResult getQueryResultForProcess(BazelProcess process) {
    return process.processBinaryOutput(unchecked(stream -> Build.QueryResult.parseFrom(stream)));
  }

  private String convertOutputToPath(String output, String prefix) {
    var pathToFile = output.replaceAll("(//|:)", "/");
    return prefix + pathToFile;
  }
}
