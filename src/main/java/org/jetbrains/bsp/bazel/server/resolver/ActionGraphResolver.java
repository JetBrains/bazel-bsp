package org.jetbrains.bsp.bazel.server.resolver;

import com.google.devtools.build.lib.analysis.AnalysisProtos;
import com.google.devtools.build.lib.analysis.AnalysisProtos.ActionGraphContainer;
import java.io.IOException;
import java.util.List;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;
import org.jetbrains.bsp.bazel.server.bazel.parameters.BazelRunnerFlag;
import org.jetbrains.bsp.bazel.server.util.ActionGraphParser;

public class ActionGraphResolver {

  private final BazelRunner bazelRunner;

  public ActionGraphResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ActionGraphParser getActionGraphParser(List<String> targets, List<String> languageIds) {
    try {
      BazelProcessResult process =
          bazelRunner
              .commandBuilder()
              .aquery()
              .withFlag(BazelRunnerFlag.OUTPUT_PROTO)
              .withMnemonic(targets, languageIds)
              .executeBazelCommand();

      ActionGraphContainer actionGraphContainer =
          AnalysisProtos.ActionGraphContainer.parseFrom(process.getStdoutStream());
      return new ActionGraphParser(actionGraphContainer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
