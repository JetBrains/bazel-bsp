package org.jetbrains.bsp.bazel.bazelrunner.params

object BazelFlag {
  @JvmStatic fun color(enabled: Boolean) =
      arg("color", if (enabled) "yes" else "no")

  @JvmStatic fun keepGoing() =
      flag("keep_going")

  @JvmStatic fun outputGroups(groups: List<String>) =
      arg("output_groups", groups.joinToString(","))

  @JvmStatic fun aspect(name: String) =
      arg("aspects", name)

  @JvmStatic fun buildManual(): String? =
    flag("build_manual_tests")

  private fun arg(name: String, value: String) =
      String.format("--%s=%s", name, value)

  private fun flag(name: String) =
      "--$name"
}
