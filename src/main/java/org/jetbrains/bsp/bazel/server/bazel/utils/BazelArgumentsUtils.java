package org.jetbrains.bsp.bazel.server.bazel.utils;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BazelArgumentsUtils {

  private static final String JOIN_TARGETS_DELIMITER = "+";
  private static final String MNEMONIC_DELIMITER = " union ";

  public static String getJoinedBazelTargets(List<String> targets) {
    String joinedTargets = joinBazelTargets(targets);
    return String.format("(%s)", joinedTargets);
  }

  public static String getMnemonicWithJoinedTargets(
      List<String> targets, List<String> languageIds) {
    String joinedTargets = joinBazelTargets(targets);
    return getMnemonicsForTargets(joinedTargets, languageIds);
  }

  private static String joinBazelTargets(List<String> targets) {
    return Joiner.on(JOIN_TARGETS_DELIMITER).join(targets);
  }

  private static String getMnemonicsForTargets(String targets, List<String> languageIds) {
    return languageIds.stream()
        .filter(Objects::nonNull)
        .map(languageId -> getMnemonicForLanguageAndTargets(languageId, targets))
        .collect(Collectors.joining(MNEMONIC_DELIMITER));
  }

  private static String getMnemonicForLanguageAndTargets(String languageId, String targets) {
    return String.format("mnemonic(%s, %s)", languageId, targets);
  }

}
