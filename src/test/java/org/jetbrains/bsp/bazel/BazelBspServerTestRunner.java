package org.jetbrains.bsp.bazel;

public class BazelBspServerTestRunner {

  public static void main(String[] args) {

    System.out.println("->> " + System.getenv("BUILD_WORKSPACE_DIRECTORY"));

    BazelBspServerTest bazelBspServerTest = new BazelBspServerTest(System.getenv("BUILD_WORKSPACE_DIRECTORY") + "/sample-repo");
    bazelBspServerTest.run();
  }
}
