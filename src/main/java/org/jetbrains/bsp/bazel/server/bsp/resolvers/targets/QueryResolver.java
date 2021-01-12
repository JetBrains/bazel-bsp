package org.jetbrains.bsp.bazel.server.bsp.resolvers.targets;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import org.jetbrains.bsp.bazel.server.bazel.data.BazelProcessResult;

public class QueryResolver {

  public static Build.QueryResult getQueryResultForProcess(BazelProcessResult process) {
    try {
      return Build.QueryResult.parseFrom(process.getStdoutStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
