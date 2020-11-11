package java.org.jetbrains.bsp.bazel.server.resolvers;

import com.google.common.base.Splitter;
import com.google.common.io.ByteStreams;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ProcessResolver {

  private final Semaphore processLock;

  private final String bazel;
  private final String besBackend;
  private final String publishAllActions;

  public ProcessResolver(Semaphore processLock, String bazel, String besBackend, String publishAllActions) {
    this.processLock = processLock;
    this.bazel = bazel;
    this.besBackend = besBackend;
    this.publishAllActions = publishAllActions;
  }

  public List<String> runBazelLines(String... args) {
    List<String> lines =
        Splitter.on("\n")
            .omitEmptyStrings()
            .splitToList(new String(runBazelBytes(args), StandardCharsets.UTF_8));
    System.out.printf("Returning: %s%n", lines);
    return lines;
  }

  public byte[] runBazelBytes(String... args) {
    try {
      Process process = startProcess(args);
      byte[] byteArray = ByteStreams.toByteArray(process.getInputStream());
      processLock.release();
      return byteArray;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> runBazelStderr(String... args) {
    try {
      Process process = startProcess(args);
      List<String> output = new ArrayList<>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        output.add(line.trim());
      }
      processLock.release();
      return output;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized Process startProcess(String... args) throws IOException, InterruptedException {
    List<String> argv = new ArrayList<>(args.length + 3);
    argv.add(bazel);
    argv.addAll(Arrays.asList(args));
    if (argv.size() > 1) {
      argv.add(2, besBackend);
      argv.add(3, publishAllActions);
    }

    processLock.acquire();
    System.out.printf("Running: %s%n", argv);
    return new ProcessBuilder(argv).start();
  }

}
