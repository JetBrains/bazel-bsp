package org.jetbrains.bsp.bazel.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BazelBspServerConfig {

  private String pathToBazel = null;
  private List<String> targetProjectPaths =
      Collections.unmodifiableList(Collections.singletonList("//..."));

  public BazelBspServerConfig(String pathToBazel) {
    this.pathToBazel = pathToBazel;
  }

  public static BazelBspServerConfig from(String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("Configuration can't be built without any parameters");
    }

    BazelBspServerConfig config = new BazelBspServerConfig(args[0]);
    if (args.length == 2) {
      config.setTargetProjectPaths(new ArrayList<>(Arrays.asList(args[1].split(","))));
    }

    return config;
  }

  public String getBazelPath() {
    return this.pathToBazel;
  }

  public List<String> getTargetProjectPaths() {
    return this.targetProjectPaths;
  }

  public BazelBspServerConfig setTargetProjectPaths(List<String> projectPaths) {
    this.targetProjectPaths = Collections.unmodifiableList(new ArrayList<>(projectPaths));
    return this;
  }
}
