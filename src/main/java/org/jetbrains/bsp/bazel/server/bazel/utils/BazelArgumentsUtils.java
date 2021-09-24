package org.jetbrains.bsp.bazel.server.bazel.utils;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.bsp.bazel.server.bazel.params.BazelQueryKindParameters;

public final class BazelArgumentsUtils {

  private static final String JOIN_TARGETS_DELIMITER = " + ";
  private static final String FUNCTIONS_DELIMITER = " union ";

  private static final String MNEMONIC_COMMAND = "mnemonic";
  private static final String KIND_COMMAND = "kind";
  private static final String EXCEPT_COMMAND = "except";

  public static String getJoinedBazelTargets(List<String> targets) {
    String joinedTargets = joinBazelTargets(targets);

    return String.format("(%s)", joinedTargets);
  }

  public static String getMnemonicWithJoinedTargets(
      List<String> targets, List<String> languageIds) {
    String joinedTargets = joinBazelTargets(targets);

    return getMnemonicsForTargets(joinedTargets, languageIds);
  }

  private static String getMnemonicsForTargets(String targets, List<String> languageIds) {
    return languageIds.stream()
        .map(languageId -> getMnemonicForLanguageAndTargets(languageId, targets))
        .collect(Collectors.joining(FUNCTIONS_DELIMITER));
  }

  public static String getQueryKindForPatternsAndExpressions(
      List<BazelQueryKindParameters> parameters) {
    return parameters.stream()
        .map(BazelArgumentsUtils::getQueryKind)
        .collect(Collectors.joining(FUNCTIONS_DELIMITER));
  }

  public static String getQueryKindForPatternsAndExpressionsWithException(
      List<BazelQueryKindParameters> parameters, String exception) {
    String kind = getQueryKindForPatternsAndExpressions(parameters);
    return String.format("%s %s %s", kind, EXCEPT_COMMAND, exception);
  }

  public static String getQueryKind(BazelQueryKindParameters parameter) {
    return String.format("%s(%s, %s)", KIND_COMMAND, parameter.getPattern(), parameter.getInput());
  }

  private static String joinBazelTargets(List<String> targets) {
    return Joiner.on(JOIN_TARGETS_DELIMITER).join(targets);
  }

  private static String getMnemonicForLanguageAndTargets(String languageId, String targets) {
    return String.format("%s(%s, %s)", MNEMONIC_COMMAND, languageId, targets);
  }
}
