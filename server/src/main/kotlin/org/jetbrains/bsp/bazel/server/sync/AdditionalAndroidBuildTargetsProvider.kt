package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.server.model.BspMappings
import org.jetbrains.bsp.bazel.server.model.Language

/**
 * Every Kotlin Android target actually produces three targets, which we merge inside [KotlinAndroidModulesMerger].
 * However, in order for all the dependent libraries to be unpacked properly (e.g. for Jetpack Compose preview),
 * we still have to pass the dependent Kotlin target explicitly during build (and not just the merged target).
 */
class AdditionalAndroidBuildTargetsProvider(private val projectProvider: ProjectProvider) {
  fun getAdditionalBuildTargets(
    cancelChecker: CancelChecker,
    targets: List<BuildTargetIdentifier>,
  ): List<BuildTargetIdentifier> {
    val project = projectProvider.get(cancelChecker)
    val modules = BspMappings.getModules(project, targets)
    return modules
      .asSequence()
      .filter { Language.ANDROID in it.languages && Language.KOTLIN in it.languages }
      .map { BuildTargetIdentifier(it.label.value + "_kt") }
      .toList()
  }
}
