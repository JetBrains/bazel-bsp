package org.jetbrains.bsp.bazel.resolvers;

import com.google.devtools.build.lib.analysis.AnalysisProtos;
import java.io.IOException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.jetbrains.bsp.bazel.ActionGraphParser;

public class ActionGraphResolver {

  private final ProcessResolver processResolver;

  public ActionGraphResolver(ProcessResolver processResolver) {
    this.processResolver = processResolver;
  }

  public Either<ResponseError, ActionGraphParser> parseActionGraph(String query) {
    try {
      AnalysisProtos.ActionGraphContainer actionGraph =
          AnalysisProtos.ActionGraphContainer.parseFrom(
              processResolver.runBazelBytes("aquery", "--output=proto", query));
      return Either.forRight(new ActionGraphParser(actionGraph));
    } catch (IOException e) {
      return Either.forLeft(
          new ResponseError(ResponseErrorCode.InternalError, e.getMessage(), null));
    }
  }
}
