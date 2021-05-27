package org.jetbrains.bsp.bazel.server.bsp.managers;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.StatusCode;
import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bep.BepServer;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.QueryResolver;
import org.jetbrains.bsp.bazel.server.bsp.utils.BuildManagerParsingUtils;

public class BazelBspCompilationManager {
  private static final Logger LOGGER = LogManager.getLogger(BazelBspCompilationManager.class);

  private BepServer bepServer;
  private final BazelRunner bazelRunner;
  private final BazelData bazelData;

  public BazelBspCompilationManager(BazelRunner bazelRunner, BazelData bazelData) {
    this.bazelRunner = bazelRunner;
    this.bazelData = bazelData;
  }

  public Either<ResponseError, CompileResult> buildTargetsWithBep(
      List<BuildTargetIdentifier> targets, List<String> extraFlags) {
    List<String> bazelTargets =
        targets.stream().map(BuildTargetIdentifier::getUri).collect(Collectors.toList());

    final Map<String, String> diagnosticsProtosLocations =
        bepServer.getDiagnosticsProtosLocations();
    BazelProcess bazelProcess =
        bazelRunner
            .commandBuilder()
            .query()
            .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
            .withTargets(bazelTargets)
            .executeBazelBesCommand();

    Build.QueryResult queryResult = QueryResolver.getQueryResultForProcess(bazelProcess);

    queryResult.getTargetList().stream()
        .map(Build.Target::getRule)
        .filter(rule -> !rule.getName().startsWith("@"))
        .forEach(
            rule ->
                rule.getRuleOutputList().stream()
                    .filter(output -> output.contains(Constants.DIAGNOSTICS))
                    .forEach(
                        output ->
                            diagnosticsProtosLocations.put(
                                rule.getName(),
                                BuildManagerParsingUtils.convertOutputToPath(
                                    output, bazelData.getBinRoot()))));

    StatusCode exitCode =
        bazelRunner
            .commandBuilder()
            .build()
            .withFlags(extraFlags)
            .withTargets(bazelTargets)
            .executeBazelBesCommand()
            .waitAndGetResult()
            .getStatusCode();

    for (Map.Entry<String, String> diagnostics : diagnosticsProtosLocations.entrySet()) {
      String target = diagnostics.getKey();
      String diagnosticsPath = diagnostics.getValue();
      BuildTargetIdentifier targetIdentifier = new BuildTargetIdentifier(target);
      // TODO (abrams27) is it ok?
      bepServer.emitDiagnostics(
          bepServer.collectDiagnostics(targetIdentifier, diagnosticsPath), targetIdentifier);
    }

    return Either.forRight(new CompileResult(exitCode));
  }

  public void setBepServer(BepServer bepServer) {
    this.bepServer = bepServer;
  }
}
