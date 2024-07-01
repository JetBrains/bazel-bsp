package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.aspect
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.buildManualTests
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.color
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.curses
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.keepGoing
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.outputGroups
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import java.nio.file.Paths

data class BazelBspAspectsManagerResult(val bepOutput: BepOutput, val isFailure: Boolean)

data class RuleLanguage(val ruleName: String?, val language: Language)

class BazelBspAspectsManager(
  private val bazelBspCompilationManager: BazelBspCompilationManager,
  private val aspectsResolver: InternalAspectsResolver,
) {

  private val aspectsPath = Paths.get(aspectsResolver.bazelBspRoot, Constants.ASPECTS_ROOT)
  private val templateWriter = TemplateWriter(aspectsPath)

  fun calculateRuleLanguages(externalRuleNames: List<String>): List<RuleLanguage> =
    Language.values().mapNotNull { language ->
      if (language.ruleNames.isEmpty()) return@mapNotNull RuleLanguage(null, language) // bundled in Bazel
      val ruleName = language.ruleNames.firstOrNull { externalRuleNames.contains(it) }
      ruleName?.let { RuleLanguage(it, language) }
    }

  fun generateAspectsFromTemplates(ruleLanguages: List<RuleLanguage>, workspaceContext: WorkspaceContext) {
    ruleLanguages.filter { it.language.isTemplate }.forEach {
      val outputFile = aspectsPath.resolve(it.language.toAspectRelativePath())
      val templateFilePath = it.language.toAspectTemplateRelativePath()
      val variableMap = mapOf(
        "ruleName" to it.ruleName,
        "addTransitiveCompileTimeJars" to if (workspaceContext.experimentalAddTransitiveCompileTimeJars.value) "True" else "False",
      )
      templateWriter.writeToFile(templateFilePath, outputFile, variableMap)
    }
  }

  fun fetchFilesFromOutputGroups(
    cancelChecker: CancelChecker,
    targetSpecs: TargetsSpec,
    aspect: String,
    outputGroups: List<String>,
    shouldBuildManualFlags: Boolean,
    isRustEnabled: Boolean,
  ): BazelBspAspectsManagerResult {
    if (targetSpecs.values.isEmpty()) return BazelBspAspectsManagerResult(BepOutput(), isFailure = false)
    val defaultFlags = listOf(
      aspect(aspectsResolver.resolveLabel(aspect)),
      outputGroups(outputGroups),
      keepGoing(),
      color(true),
      curses(false),
    )
    val buildManualTargetsFlags = if (shouldBuildManualFlags) listOf(buildManualTests()) else emptyList()

    val flagsToUse = defaultFlags + buildManualTargetsFlags

    return bazelBspCompilationManager
      .buildTargetsWithBep(
        cancelChecker = cancelChecker,
        targetSpecs = targetSpecs,
        extraFlags = flagsToUse,
        originId = null,
        // Setting `CARGO_BAZEL_REPIN=1` updates `cargo_lockfile`
        // (`Cargo.lock` file) based on dependencies specified in `manifest`
        // (`Cargo.toml` file) and syncs `lockfile` (`Cargo.bazel.lock` file) with `cargo_lockfile`.
        // Ensures that both Bazel and Cargo are using the same versions of dependencies.
        // Mentioned `cargo_lockfile`, `lockfile` and `manifest` are defined in
        // `crates_repository` from `rules_rust`,
        // see: https://bazelbuild.github.io/rules_rust/crate_universe.html#crates_repository.
        // In our server used only with `bazel build` command.
        environment = if (isRustEnabled) listOf(Pair("CARGO_BAZEL_REPIN", "1")) else emptyList(),
      ).let {
        BazelBspAspectsManagerResult(it.bepOutput, it.processResult.isNotSuccess)
      }
  }
}
