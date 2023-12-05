package org.jetbrains.bsp.bazel.server.bsp.managers

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.aspect
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.buildManualTests
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.color
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.curses
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.keepGoing
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.outputGroups
import org.jetbrains.bsp.bazel.bazelrunner.params.BazelFlag.repositoryOverride
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bep.BepOutput
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.workspacecontext.TargetsSpec
import java.io.StringWriter
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.writeText

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

  fun generateAspectsFromTemplates(ruleLanguages: List<RuleLanguage>) {
    ruleLanguages.filter { it.ruleName != null && it.language.isTemplate }.forEach {
      val outputFile = aspectsPath.resolve(it.language.toAspectRelativePath())
      val templateFilePath = it.language.toAspectTemplateRelativePath()
      templateWriter.writeToFile(templateFilePath, outputFile, mapOf("ruleName" to it.ruleName))
    }
  }

  fun fetchFilesFromOutputGroups(
    cancelChecker: CancelChecker,
    targetSpecs: TargetsSpec,
    aspect: String,
    outputGroups: List<String>,
  ): BazelBspAspectsManagerResult {

    return if (targetSpecs.values.isEmpty()) BazelBspAspectsManagerResult(BepOutput(), isFailure = false)
    else bazelBspCompilationManager
      .buildTargetsWithBep(
        cancelChecker,
        targetSpecs,
        listOf(
          repositoryOverride(
            Constants.ASPECT_REPOSITORY, aspectsResolver.bazelBspRoot
          ),
          aspect(aspectsResolver.resolveLabel(aspect)),
          outputGroups(outputGroups),
          keepGoing(),
          color(true),
          buildManualTests(),
          curses(false)
        ),
        null
      ).let {
        BazelBspAspectsManagerResult(it.bepOutput, it.processResult.isNotSuccess)
      }
  }
}
