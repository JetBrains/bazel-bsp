package org.jetbrains.bsp.bazel.server.utils;

import com.google.common.base.Joiner;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MnemonicsUtils {

  private static final String TARGETS_UNION_SEPARATOR = " + ";

  public static String getMnemonics(List<String> targets, List<String> languageIds) {
    String targetsUnion = Joiner.on(TARGETS_UNION_SEPARATOR).join(targets);

    return languageIds.stream()
        .filter(Objects::nonNull)
        .map(mnemonic -> "mnemonic(" + mnemonic + ", " + targetsUnion + ")")
        .collect(Collectors.joining(" union "));
  }
}
