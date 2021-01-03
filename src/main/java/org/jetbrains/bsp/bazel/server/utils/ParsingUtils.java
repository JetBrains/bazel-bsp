package org.jetbrains.bsp.bazel.server.utils;

import ch.epfl.scala.bsp4j.BuildTargetTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.bsp.bazel.commons.Constants;

public class ParsingUtils {

  private static final Logger LOGGER = LogManager.getLogger(ParsingUtils.class);

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

  public static String getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(0, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version = version.substring(0, dot);
      }
    }
    return version;
  }

}
