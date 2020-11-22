package org.jetbrains.bsp.bazel.server.resolvers;

import com.google.devtools.build.lib.analysis.AnalysisProtos;
import com.google.devtools.build.lib.analysis.AnalysisProtos.ActionGraphContainer;
import java.io.IOException;
import org.jetbrains.bsp.bazel.common.ActionGraphParser;
import org.jetbrains.bsp.bazel.server.data.ProcessResults;

public class ActionGraphResolver {

  private final BazelRunner bazelRunner;

  public ActionGraphResolver(BazelRunner bazelRunner) {
    this.bazelRunner = bazelRunner;
  }

  public ActionGraphParser parseActionGraph(String query) {
    try {
      ProcessResults process = bazelRunner.runBazelCommand("aquery", "--output=proto", query);
      ActionGraphContainer actionGraphContainer =
          AnalysisProtos.ActionGraphContainer.parseFrom(process.getStdoutStream());
      return new ActionGraphParser(actionGraphContainer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
