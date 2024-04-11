package org.jetbrains.bsp.bazel.server.sync.languages.android

import org.jetbrains.bsp.bazel.server.sync.languages.jvm.javaModule
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinModule
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.isAndroidEnabled

/**
 * kt_android_library/kt_android_local_test with the name `foo` actually produces three targets:
 * - An android_library target `foo_base` with Android resources;
 * - A kt_jvm_library target `foo_kt` with Kotlin sources that depends on `foo_base`;
 * - An empty target `foo` that just depends on the two targets above.
 *
 * This approach creates several problems:
 * - References from `foo_base` to `foo_kt` are not resolved - we would have to add a circular dependency;
 * - Test classes are inside `foo_kt`, so the BSP client calls buildTargetTest for `foo_kt` instead of `foo`;
 * - Three targets are displayed for the user instead of one that they defined in the BUILD file.
 *
 * Therefore, we just merge them all into a single target.
 * Also see [Android Kotlin rules implementation](https://github.com/bazelbuild/rules_kotlin/blob/a675511fdbee743c09d537c2dddfb349981ae70b/kotlin/internal/jvm/android.bzl).
 */
class KotlinAndroidModulesMerger {
  fun mergeKotlinAndroidModules(modules: List<Module>, workspaceContext: WorkspaceContext): List<Module> =
    if (workspaceContext.isAndroidEnabled) doMergeKotlinAndroidModules(modules) else modules

  private fun doMergeKotlinAndroidModules(modules: List<Module>): List<Module> {
    val moduleById = modules.associateBy { it.label.value }

    val mergedKotlinAndroidModules = modules.mapNotNull { parentModule ->
      tryMergeKotlinAndroidModule(parentModule, moduleById)
    }
    if (mergedKotlinAndroidModules.isEmpty()) return modules

    val newModules = mergedKotlinAndroidModules.map { it.mergedModule }
    val oldModulesToRemove = mergedKotlinAndroidModules.asSequence()
      .flatMap { it.oldModulesToRemove }
      .map { it.label.value }
      .toSet()
    val withoutOldModules = (moduleById - oldModulesToRemove).values
    return withoutOldModules + newModules
  }

  private fun tryMergeKotlinAndroidModule(
    parentModule: Module,
    moduleById: Map<String, Module>,
  ): MergedKotlinAndroidModule? {
    if (parentModule.sourceSet.sources.isNotEmpty()) return null

    val parentModuleId = parentModule.label.value
    val kotlinModule = moduleById[parentModuleId + "_kt"] ?: return null
    val androidModule = moduleById[parentModuleId + "_base"] ?: return null

    val kotlinLanguageData = kotlinModule.languageData
    if (kotlinLanguageData !is KotlinModule?) return null

    // kt_android_local_test passes the resources and manifest to the parent module;
    // kt_android_library passes them to the Android module.
    val androidLanguageData = if (androidModule.resources.isNotEmpty()) {
      androidModule.languageData as? AndroidModule
    } else {
      parentModule.languageData as? AndroidModule
    } ?: return null

    val javaModule = androidLanguageData.javaModule?.run {
      copy(binaryOutputs = binaryOutputs + kotlinModule.javaModule?.binaryOutputs.orEmpty())
    }
    val kotlinAndroidLanguageData = androidLanguageData.copy(kotlinModule = kotlinLanguageData, javaModule = javaModule)

    val mergedDependencies =
      kotlinModule.directDependencies.asSequence() + androidModule.directDependencies.asSequence()
    val correctMergedDependencies = mergedDependencies
      .filterNot { it == androidModule.label }
      .filterNot { it == kotlinModule.label }
      .filterNot { it.value.endsWith("//third_party:android_sdk") }  // This is added by Kotlin rules
      .distinct()
      .toList()

    val mergedModule = Module(
      label = parentModule.label,
      isSynthetic = false,
      directDependencies = correctMergedDependencies,
      languages = kotlinModule.languages + androidModule.languages,
      tags = parentModule.tags,
      baseDirectory = parentModule.baseDirectory,
      sourceSet = kotlinModule.sourceSet,
      resources = androidModule.resources + parentModule.resources,
      outputs = kotlinModule.outputs + androidModule.outputs,
      sourceDependencies = kotlinModule.sourceDependencies + androidModule.sourceDependencies,
      languageData = kotlinAndroidLanguageData,
      environmentVariables = kotlinModule.environmentVariables + androidModule.environmentVariables,
    )

    return MergedKotlinAndroidModule(
      mergedModule = mergedModule,
      oldModulesToRemove = listOf(parentModule, kotlinModule, androidModule),
    )
  }

  private data class MergedKotlinAndroidModule(
    val mergedModule: Module,
    val oldModulesToRemove: List<Module>,
  )
}
