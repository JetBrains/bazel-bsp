package org.jetbrains.bsp.bazel.server.bsp.resolvers;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import org.jetbrains.bsp.bazel.server.bazel.BazelProcess;

public class QueryResolver {

  public static Build.QueryResult getQueryResultForProcess(BazelProcess process) {
    try {
      return Build.QueryResult.parseFrom(process.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
