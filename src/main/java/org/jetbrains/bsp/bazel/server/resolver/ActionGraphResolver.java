package org.jetbrains.bsp.bazel.server.resolver;

import com.google.devtools.build.lib.analysis.AnalysisProtos;
import com.google.devtools.build.lib.analysis.AnalysisProtos.ActionGraphContainer;
import java.io.IOException;
import java.util.List;
import org.jetbrains.bsp.bazel.common.ActionGraphParser;
import org.jetbrains.bsp.bazel.server.bazel.BazelActionGraphQueryRunner;
import org.jetbrains.bsp.bazel.server.bazel.ProcessResults;
import org.jetbrains.bsp.bazel.server.bazel.BazelRunner;
import org.jetbrains.bsp.bazel.server.data.ProcessResults;
import org.jetbrains.bsp.bazel.server.util.ActionGraphParser;

public class ActionGraphResolver {

  private final BazelActionGraphQueryRunner bazelActionGraphQueryRunner;

  public ActionGraphResolver(BazelRunner bazelRunner) {
    this.bazelActionGraphQueryRunner = new BazelActionGraphQueryRunner(bazelRunner);
  }

  public ActionGraphParser parseActionGraph(List<String> targets, List<String> languageIds) {
    try {
      ProcessResults process = bazelActionGraphQueryRunner.aquery(targets, languageIds);
      ActionGraphContainer actionGraphContainer =
          AnalysisProtos.ActionGraphContainer.parseFrom(process.getStdoutStream());
      return new ActionGraphParser(actionGraphContainer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
