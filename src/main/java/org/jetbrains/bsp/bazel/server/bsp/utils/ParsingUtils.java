package org.jetbrains.bsp.bazel.server.bsp.utils;

import ch.epfl.scala.bsp4j.BuildTargetTag;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.jetbrains.bsp.bazel.commons.Constants;

public class ParsingUtils {

  private static final Map<String, String> RULE_CLASS_TO_RULE_TYPE = ImmutableMap.of(
      Constants.LIBRARY_RULE_TYPE, BuildTargetTag.LIBRARY,
      Constants.BINARY_RULE_TYPE, BuildTargetTag.APPLICATION,
      Constants.TEST_RULE_TYPE, BuildTargetTag.TEST
  );
  private static final String DEFAULT_RULE_TYPE = BuildTargetTag.NO_IDE;

  public static String convertOutputToPath(String output, String prefix) {
    String pathToFile = output.replaceAll("(//|:)", "/");
    return prefix + pathToFile;
  }

  public static String getRuleType(String ruleClass) {
    return RULE_CLASS_TO_RULE_TYPE.getOrDefault(ruleClass, DEFAULT_RULE_TYPE);
  }
}
