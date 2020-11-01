package org.jetbrains.bsp.bazel;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MnemonicsUtils {

  public static String getMnemonics(String targetsUnion, List<String> languageIds) {
    return languageIds.stream()
        .filter(Objects::nonNull)
        .map(mnemonic -> "mnemonic(" + mnemonic + ", " + targetsUnion + ")")
        .collect(Collectors.joining(" union "));
  }

}
