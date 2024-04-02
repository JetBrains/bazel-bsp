package org.jetbrains.bsp.bazel.server.sync

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManagerResult
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspFallbackAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspLanguageExtensionsGenerator
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelExternalRulesQueryImpl
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

/** Responsible for querying bazel and constructing Project instance  */
class ProjectResolver(
  private val bazelBspAspectsManager: BazelBspAspectsManager,
  private val bazelBspLanguageExtensionsGenerator: BazelBspLanguageExtensionsGenerator,
  private val bazelBspFallbackAspectsManager: BazelBspFallbackAspectsManager,
  private val workspaceContextProvider: WorkspaceContextProvider,
  private val bazelProjectMapper: BazelProjectMapper,
  private val bspLogger: BspClientLogger,
  private val targetInfoReader: TargetInfoReader,
  private val bazelInfo: BazelInfo,
  private val bazelRunner: BazelRunner,
  private val metricsLogger: MetricsLogger?
) {
  private fun <T> measured(description: String, f: () -> T): T {
    return Measurements.measure(f, description, metricsLogger, bspLogger)
  }

  fun resolve(cancelChecker: CancelChecker, build: Boolean): Project {

    val workspaceContext = measured(
      "Reading project view and creating workspace context",
      workspaceContextProvider::currentWorkspaceContext
    )

    val bazelExternalRulesQuery =
      BazelExternalRulesQueryImpl(bazelRunner, bazelInfo.isBzlModEnabled, workspaceContext.enabledRules)

    val externalRuleNames = measured(
      "Discovering supported external rules"
    ) { bazelExternalRulesQuery.fetchExternalRuleNames(cancelChecker) }

    val ruleLanguages = measured(
      "Mapping rule names to languages"
    ) { bazelBspAspectsManager.calculateRuleLanguages(externalRuleNames) }

    measured("Realizing language aspect files from templates") {
      bazelBspAspectsManager.generateAspectsFromTemplates(ruleLanguages)
    }

    measured("Generating language extensions file") {
      bazelBspLanguageExtensionsGenerator.generateLanguageExtensions(ruleLanguages)
    }

    val buildAspectResult = measured(
      "Building project with aspect"
    ) { buildProjectWithAspect(cancelChecker, workspaceContext, keepDefaultOutputGroups = build) }
    val aspectOutputs = measured(
        "Reading aspect output paths"
    ) { buildAspectResult.bepOutput.filesByOutputGroupNameTransitive(BSP_INFO_OUTPUT_GROUP) }
    val targets = measured(
        "Parsing aspect outputs"
    ) { targetInfoReader.readTargetMapFromAspectOutputs(aspectOutputs).let { it } }
    val allTargetNames =
      if (buildAspectResult.isFailure)
        measured(
          "Fetching all possible target names"
        ) { formatTargetsIfNeeded(bazelBspFallbackAspectsManager.getAllPossibleTargets(cancelChecker), targets) }
      else
        emptyList()
    val rootTargets = buildAspectResult.bepOutput.rootTargets().let { formatTargetsIfNeeded(it, targets) }
    return measured(
      "Mapping to internal model"
    ) { bazelProjectMapper.createProject(targets, rootTargets.toSet(), allTargetNames, workspaceContext, bazelInfo) }
  }

  private fun buildProjectWithAspect(cancelChecker: CancelChecker, workspaceContext: WorkspaceContext, keepDefaultOutputGroups: Boolean): BazelBspAspectsManagerResult {
    val outputGroups =
      listOf(BSP_INFO_OUTPUT_GROUP, ARTIFACTS_OUTPUT_GROUP, RUST_ANALYZER_OUTPUT_GROUP)
        .map { if (keepDefaultOutputGroups) "+$it" else it }

    return bazelBspAspectsManager.fetchFilesFromOutputGroups(
      cancelChecker = cancelChecker,
      targetSpecs = workspaceContext.targets,
      aspect = ASPECT_NAME,
      outputGroups = outputGroups,
      shouldBuildManualFlags = workspaceContext.shouldAddBuildAffectingFlags(keepDefaultOutputGroups)
    )
  }

  private fun formatTargetsIfNeeded(targets: Collection<String>, targetsInfo: Map<String, BspTargetInfo.TargetInfo >): List<String> =
    when (bazelInfo.release.major) {
      // Since bazel 6, the main repository targets are stringified to "@//"-prefixed labels,
      // contrary to "//"-prefixed in older Bazel versions. Unfortunately this does not apply
      // to BEP data, probably due to a bug, so we need to add the "@" or "@@" prefix here.
      in 0..5 -> targets.toList()
      else -> targets.map {
          if (targetsInfo.contains("@@$it")) "@@$it"
          else "@$it"
        }
    }.toList()

  private fun WorkspaceContext.shouldAddBuildAffectingFlags(willBeBuilt: Boolean): Boolean =
    this.buildManualTargets.value || !willBeBuilt

  companion object {
    private const val ASPECT_NAME = "bsp_target_info_aspect"
    private const val BSP_INFO_OUTPUT_GROUP = "bsp-target-info-transitive-deps"
    private const val ARTIFACTS_OUTPUT_GROUP = "external-deps-resolve-transitive-deps"
    private const val RUST_ANALYZER_OUTPUT_GROUP = "rust_analyzer_crate_spec"
  }
}
