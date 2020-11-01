package org.jetbrains.bsp.bazel;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class QueryResolver {

  private final ProcessRunner processRunner;
  private final Semaphore processLock;

  public QueryResolver(ProcessRunner processRunner, Semaphore processLock) {
    this.processRunner = processRunner;
    this.processLock = processLock;
  }

  public Build.QueryResult getQuery(String... args) throws IOException {
    try {
      Process process = processRunner.startProcess(args);

      Build.QueryResult queryResult = Build.QueryResult.parseFrom(process.getInputStream());
      processLock.release();
      return queryResult;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
