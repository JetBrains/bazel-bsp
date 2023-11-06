package org.jetbrains.bsp.bazel.server.bsp.managers

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import java.io.StringWriter
import java.nio.file.Paths
import java.util.Properties
import kotlin.io.path.writeText

enum class Language(private val fileName: String, val functions: List<String>, val isTemplate: Boolean) {
  Java("//aspects:rules/java/java_info.bzl", listOf("extract_java_toolchain", "extract_java_runtime"), false),
  Jvm("//aspects:rules/jvm/jvm_info.bzl", listOf("extract_jvm_info"), false),
  Scala("//aspects:rules/scala/scala_info.bzl", listOf("extract_scala_info", "extract_scala_toolchain_info"), false),
  Cpp("//aspects:rules/cpp/cpp_info.bzl", listOf("extract_cpp_info"), false),
  Kotlin("//aspects:rules/kt/kt_info.bzl", listOf("extract_kotlin_info"), true),
  Python("//aspects:rules/python/python_info.bzl", listOf("extract_python_info"), false);

  fun toLoadStatement(): String =
    this.functions.joinToString(
      prefix = """load("${this.fileName}", """,
      separator = ", ",
      postfix = ")"
    ) { "\"$it\"" }

  fun toAspectRelativePath(): String =
    fileName.substringAfter(":")

  fun toAspectTemplateRelativePath(): String =
    "${toAspectRelativePath()}.template"
}

class BazelBspEnvironmentManager(
  private val internalAspectsResolver: InternalAspectsResolver,
  private val bazelExternalRulesQuery: BazelExternalRulesQuery,
) {

  private val aspectsPath = Paths.get(internalAspectsResolver.bazelBspRoot, Constants.ASPECTS_ROOT)
  private val velocityEngine = VelocityEngine()

  init {
    val props = calculateProperties()
    velocityEngine.init(props)
  }

  private fun calculateProperties(): Properties {
    val props = Properties()
    props["file.resource.loader.path"] = aspectsPath.toAbsolutePath().toString()
    props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem")
    return props
  }

  private data class RuleLanguage(val ruleNames: List<String>, val language: Language)

  fun generateLanguageExtensions(cancelChecker: CancelChecker) {
    val allExternalRuleNames = bazelExternalRulesQuery.fetchExternalRuleNames(cancelChecker)
    val ruleLanguages = getProjectRuleLanguages(allExternalRuleNames)
    makeAspectFilesFromTemplates(ruleLanguages)

    val fileContent = prepareFileContent(ruleLanguages)

    createNewExtensionsFile(fileContent)
  }

  private fun getProjectRuleLanguages(allExternalRuleNames: List<String>): List<RuleLanguage> {

    val ruleLanguages = listOf(
      RuleLanguage(listOf(), Language.Java),  // Bundled in Bazel
      RuleLanguage(listOf(), Language.Jvm),  // Bundled in Bazel
      RuleLanguage(listOf(), Language.Python), // Bundled in Bazel
      RuleLanguage(listOf("rules_cc"), Language.Cpp),
      RuleLanguage(listOf("io_bazel_rules_kotlin", "rules_kotlin"), Language.Kotlin),
      RuleLanguage(listOf("io_bazel_rules_scala"), Language.Scala)
    )

    return ruleLanguages.mapNotNull { (ruleNames, language) ->
      if (ruleNames.isEmpty()) return@mapNotNull RuleLanguage(listOf(), language)
      val ruleName = ruleNames.firstOrNull { allExternalRuleNames.contains(it) }
      if (ruleName != null)
        RuleLanguage(listOf(ruleName), language)
      else null
    }
  }

  private fun makeAspectFilesFromTemplates(ruleLanguages: List<RuleLanguage>) {
    ruleLanguages.forEach {
      if (it.ruleNames.isEmpty() || !it.language.isTemplate) return@forEach
      val file = aspectsPath.resolve(it.language.toAspectRelativePath())
      val template = velocityEngine.getTemplate(it.language.toAspectTemplateRelativePath())
      val context = VelocityContext()
      context.put("ruleName", it.ruleNames.first())
      val writer = StringWriter()
      template.merge(context, writer)
      file.writeText(writer.toString())
    }
  }

  private fun prepareFileContent(ruleLanguages: List<RuleLanguage>) =
    listOf(
      "# This is a generated file, do not edit it",
      createLoadStatementsString(ruleLanguages.map { it.language }),
      createExtensionListString(ruleLanguages.map { it.language }),
      createToolchainListString(ruleLanguages)
    ).joinToString(
      separator = "\n",
      postfix = "\n"
    )

  private fun createLoadStatementsString(languages: List<Language>): String {
    val loadStatements = languages.map { it.toLoadStatement() }
    return loadStatements.joinToString(postfix = "\n", separator = "\n")
  }

  private fun createExtensionListString(languages: List<Language>): String {
    val functionNames = languages.flatMap { it.functions }
    return functionNames.joinToString(prefix = "EXTENSIONS = [\n", postfix = "\n]", separator = ",\n ") { "\t$it" }
  }

  private fun createToolchainListString(ruleLanguages: List<RuleLanguage>): String =
    ruleLanguages.mapNotNull {
      when (it.language) {
        Language.Scala -> """"@${it.ruleNames.first()}//scala:toolchain_type""""
        Language.Java -> """"@bazel_tools//tools/jdk:runtime_toolchain_type""""
        Language.Kotlin -> """"@${it.ruleNames.first()}//kotlin/internal:kt_toolchain_type""""
        else -> null
      }
    }
      .joinToString(prefix = "TOOLCHAINS = [\n", postfix = "\n]", separator = ",\n ") { "\t$it" }


  private fun createNewExtensionsFile(fileContent: String) {
    val file = aspectsPath.resolve("extensions.bzl")
    file.writeText(fileContent)
  }
}
