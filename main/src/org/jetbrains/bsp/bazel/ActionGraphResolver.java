package org.jetbrains.bsp.bazel;

import com.google.devtools.build.lib.analysis.AnalysisProtos;
import java.io.IOException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;

public class ActionGraphResolver {

  private final ProcessRunner processRunner;

  public ActionGraphResolver(ProcessRunner processRunner) {
    this.processRunner = processRunner;
  }

  public Either<ResponseError, ActionGraphParser> parseActionGraph(String query) {
    try {
      AnalysisProtos.ActionGraphContainer actionGraph =
          AnalysisProtos.ActionGraphContainer.parseFrom(
              processRunner.runBazelBytes("aquery", "--output=proto", query));
      return Either.forRight(new ActionGraphParser(actionGraph));
    } catch (IOException e) {
      return Either.forLeft(
          new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
    }
  }
}
