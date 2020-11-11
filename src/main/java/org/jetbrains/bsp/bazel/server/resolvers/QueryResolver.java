package java.org.jetbrains.bsp.bazel.server.resolvers;

import com.google.devtools.build.lib.query2.proto.proto2api.Build;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class QueryResolver {

  private final ProcessResolver processResolver;
  private final Semaphore processLock;

  public QueryResolver(ProcessResolver processResolver, Semaphore processLock) {
    this.processResolver = processResolver;
    this.processLock = processLock;
  }

  public Build.QueryResult getQuery(String... args) throws IOException {
    try {
      Process process = processResolver.startProcess(args);

      Build.QueryResult queryResult = Build.QueryResult.parseFrom(process.getInputStream());
      processLock.release();
      return queryResult;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
