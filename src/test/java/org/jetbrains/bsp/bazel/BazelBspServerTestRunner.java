package org.jetbrains.bsp.bazel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BazelBspServerTestRunner {

  private static Logger logger = LogManager.getLogger(BazelBspServerTestRunner.class);

  public static void main(String[] args) {
    logger.info("Starting BazelBspServerTest...");

    BazelBspServerTest bazelBspServerTest = new BazelBspServerTest();
    bazelBspServerTest.run();
  }
}
