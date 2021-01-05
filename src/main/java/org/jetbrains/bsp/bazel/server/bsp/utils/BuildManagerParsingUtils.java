package org.jetbrains.bsp.bazel.server.bsp.utils;

import ch.epfl.scala.bsp4j.BuildTargetTag;
import org.jetbrains.bsp.bazel.commons.Constants;

public class BuildManagerParsingUtils {

  public static String convertOutputToPath(String output, String prefix) {
    String pathToFile = output.replaceAll("(//|:)", "/");
    return prefix + pathToFile;
  }

  public static String getRuleType(String ruleClass) {
    if (ruleClass.contains(Constants.LIBRARY_RULE_TYPE)) {
      return BuildTargetTag.LIBRARY;
    }

    if (ruleClass.contains(Constants.BINARY_RULE_TYPE)) {
      return BuildTargetTag.APPLICATION;
    }

    if (ruleClass.contains(Constants.TEST_RULE_TYPE)) {
      return BuildTargetTag.TEST;
    }

    return BuildTargetTag.NO_IDE;
  }
}
