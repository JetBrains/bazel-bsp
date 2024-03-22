package org.jetbrains.bsp.bazel.server.sync.languages.android

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.StatusCode
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelArgumentsUtils.getJoinedBazelTargets
import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelArgumentsUtils.toRawUris

/**
 * Every Kotlin Android target actually produces three targets, which we merge inside [KotlinAndroidModulesMerger].
 * However, in order for all the dependent libraries to be unpacked properly (e.g. for Jetpack Compose preview),
 * we still have to pass the dependent Kotlin target explicitly during build (and not just the merged target).
 */
object AdditionalAndroidBuildTargetsQuery {
  fun additionalAndroidBuildTargetsQuery(
    targets: List<BuildTargetIdentifier>,
    bazelRunner: BazelRunner,
    cancelChecker: CancelChecker,
  ): List<BuildTargetIdentifier> {
    val targetUris = toRawUris(targets)
    val joinedTargets = getJoinedBazelTargets(targetUris)
    val childTargetsResult = bazelRunner.commandBuilder().query()
      .withArgument("""kind("kt_jvm_library", deps(kind("android_", $joinedTargets), 1))""")
      .executeBazelCommand(null).waitAndGetResult(cancelChecker)

    if (childTargetsResult.statusCode != StatusCode.OK) {
      error("Could not retrieve additional android build targets")
    }

    val targetUriSet = targetUris.asSequence().map { it.trimStart('@') }.toSet()

    return childTargetsResult.stdoutLines
      .asSequence()
      .filter { it.endsWith("_kt") }
      .filter { it.dropLast(3) in targetUriSet }
      .map { BuildTargetIdentifier(it) }
      .toList()
  }
}
