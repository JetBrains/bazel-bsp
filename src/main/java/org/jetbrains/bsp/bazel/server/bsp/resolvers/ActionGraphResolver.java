package org.jetbrains.bsp.bazel.server.bsp.resolvers;

import com.google.devtools.build.lib.analysis.AnalysisProtos;
import com.google.devtools.build.lib.analysis.AnalysisProtosV2;
import java.io.IOException;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelData;
import org.jetbrains.bsp.bazel.server.bazel.data.SemanticVersion;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph.ActionGraphParser;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph.ActionGraphV1Parser;
import org.jetbrains.bsp.bazel.server.bsp.resolvers.actiongraph.ActionGraphV2Parser;

public class ActionGraphResolver {

  private final BazelRunner bazelRunner;
  private final BazelData bazelData;
  private static final SemanticVersion ACTION_GRAPH_V2_VERSION = new SemanticVersion("4.0.0");

  public ActionGraphResolver(BazelRunner bazelRunner, BazelData bazelData) {
    this.bazelRunner = bazelRunner;
    this.bazelData = bazelData;
  }

  public ActionGraphParser getActionGraphParser(List<String> targets, List<String> languageIds) {
    try {
      BazelProcess process =
          bazelRunner
              .commandBuilder()
              .aquery()
              .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
              .withMnemonic(targets, languageIds)
              .executeBazelBesCommand();

      if (bazelData.getVersion().compareTo(ACTION_GRAPH_V2_VERSION) < 0) {
        AnalysisProtos.ActionGraphContainer actionGraphContainer =
            AnalysisProtos.ActionGraphContainer.parseFrom(process.getInputStream());
        return new ActionGraphV1Parser(actionGraphContainer);
      } else {
        AnalysisProtosV2.ActionGraphContainer actionGraphContainer =
            AnalysisProtosV2.ActionGraphContainer.parseFrom(process.getInputStream());
        return new ActionGraphV2Parser(actionGraphContainer);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
