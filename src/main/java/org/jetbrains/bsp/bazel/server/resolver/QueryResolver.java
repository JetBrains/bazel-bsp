package org.jetbrains.bsp.bazel.server.resolver;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import org.jetbrains.bsp.bazel.server.bazel.data.ProcessResults;

public class QueryResolver {

  public static Build.QueryResult getQueryResultForProcess(ProcessResults process) {
    try {
      return Build.QueryResult.parseFrom(process.getStdoutStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
