package org.jetbrains.bsp.bazel.bazelrunner.params

import org.jetbrains.bsp.bazel.commons.Constants.NAME
import org.jetbrains.bsp.bazel.commons.Constants.VERSION

object BazelFlag {
  @JvmStatic fun color(enabled: Boolean) =
      arg("color", if (enabled) "yes" else "no")

  @JvmStatic fun keepGoing() =
      flag("keep_going")

  @JvmStatic fun outputGroups(groups: List<String>) =
      arg("output_groups", groups.joinToString(","))

  @JvmStatic fun aspect(name: String) =
      arg("aspects", name)

  @JvmStatic fun buildManualTests(): String =
          flag("build_manual_tests")

  @JvmStatic fun buildEventBinaryPathConversion(enabled: Boolean): String =
          arg("build_event_binary_file_path_conversion", enabled.toString())

  @JvmStatic fun curses(enabled: Boolean): String =
          arg("curses", if (enabled) "yes" else "no")

  @JvmStatic fun repositoryOverride(repositoryName: String, path: String): String =
    arg("override_repository", "$repositoryName=$path")

  @JvmStatic fun testOutputAll(): String =
    arg("test_output", "all")

  @JvmStatic fun experimentalGoogleLegacyApi(): String =
      flag("experimental_google_legacy_api")

  @JvmStatic fun experimentalEnableAndroidMigrationApis(): String =
    flag("experimental_enable_android_migration_apis")

  @JvmStatic fun device(device: String): String =
    arg("device", device)

  @JvmStatic fun start(startType: String): String =
    arg("start", startType)

  @JvmStatic fun testFilter(filterExpression: String): String =
          arg("test_filter", filterExpression)

  @JvmStatic fun toolTag(): String =
    arg("tool_tag", "$NAME:$VERSION")

  private fun arg(name: String, value: String) =
      String.format("--%s=%s", name, value)

  private fun flag(name: String) =
      "--$name"
}
