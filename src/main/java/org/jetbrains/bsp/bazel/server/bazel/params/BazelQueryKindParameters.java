package org.jetbrains.bsp.bazel.server.bazel.params;

public class BazelQueryKindParameters {

  private final String pattern;
  private final String input;

  private BazelQueryKindParameters(String pattern, String input) {
    this.pattern = pattern;
    this.input = input;
  }

  public static BazelQueryKindParameters fromPatternAndInput(String pattern, String input) {
    return new BazelQueryKindParameters(pattern, input);
  }

  public String getPattern() {
    return pattern;
  }

  public String getInput() {
    return input;
  }
}
