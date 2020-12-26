package org.jetbrains.bsp.bazel.server.bsp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BazelBspServerConfig {

  private final String pathToBazel;
  private final List<String> targetProjectPaths;

  public BazelBspServerConfig(String pathToBazel, List<String> targetProjectPaths) {
    this.pathToBazel = pathToBazel;
    this.targetProjectPaths = targetProjectPaths;
  }

  public static BazelBspServerConfig from(String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("Configuration can't be built without any parameters");
    }

    String pathToBazel = args[0];
    List<String> targetProjectPaths =
        args.length == 2
            ? Arrays.asList(args[1].split(","))
            : Collections.unmodifiableList(Collections.singletonList("//..."));

    return new BazelBspServerConfig(pathToBazel, targetProjectPaths);
  }

  public String getBazelPath() {
    return pathToBazel;
  }

  public List<String> getTargetProjectPaths() {
    return targetProjectPaths;
  }
}
