package org.jetbrains.bsp.bazel.server.bsp.managers

import org.apache.velocity.app.VelocityEngine
import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import java.nio.file.Paths
import java.util.Properties
import kotlin.io.path.writeText

enum class Language(private val fileName: String, val ruleNames: List<String>, val functions: List<String>, val isTemplate: Boolean) {
  Java("//aspects:rules/java/java_info.bzl", listOf(), listOf("extract_java_toolchain", "extract_java_runtime"), false),
  Jvm("//aspects:rules/jvm/jvm_info.bzl", listOf(), listOf("extract_jvm_info"), false),
  Python("//aspects:rules/python/python_info.bzl", listOf(), listOf("extract_python_info"), false),
  Scala("//aspects:rules/scala/scala_info.bzl", listOf("io_bazel_rules_scala"), listOf("extract_scala_info"), false),
  Cpp("//aspects:rules/cpp/cpp_info.bzl", listOf("rules_cc"), listOf("extract_cpp_info"), false),
  Kotlin("//aspects:rules/kt/kt_info.bzl", listOf("io_bazel_rules_kotlin", "rules_kotlin"), listOf("extract_kotlin_info"), true),
  Rust("//aspects:rules/rust/rust_info.bzl", listOf("rules_rust"), listOf("extract_rust_crate_info"), false),
  Android("//aspects:rules/android/android_info.bzl", listOf("rules_android"), listOf("extract_android_info", "extract_android_aar_import_info"), false),
  Go("//aspects:rules/go/go_info.bzl", listOf("rules_go", "io_bazel_rules_go"), listOf("extract_go_info"), true);

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

class BazelBspLanguageExtensionsGenerator(internalAspectsResolver: InternalAspectsResolver, private val bazelRelease: BazelRelease) {

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

  fun generateLanguageExtensions(ruleLanguages: List<RuleLanguage>) {
    val fileContent = prepareFileContent(ruleLanguages)
    createNewExtensionsFile(fileContent)
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
        Language.Scala -> """"@${it.ruleName}//scala:toolchain_type""""
        Language.Java -> """"@bazel_tools//tools/jdk:runtime_toolchain_type""""
        Language.Kotlin -> """"@${it.ruleName}//kotlin/internal:kt_toolchain_type""""
        Language.Rust -> """"@${it.ruleName}//rust:toolchain_type""""
        Language.Android -> getAndroidToolchain()
        Language.Go -> """"@${it.ruleName}//go:toolchain""""
        else -> null
      }
    }
      .joinToString(prefix = "TOOLCHAINS = [\n", postfix = "\n]", separator = ",\n ") { "\t$it" }

  private fun getAndroidToolchain(): String? = when (bazelRelease.major) {
    in 0..5 -> null  // No support for optional toolchains
    else -> """config_common.toolchain_type("@bazel_tools//tools/android:sdk_toolchain_type", mandatory = False)"""
  }

  private fun createNewExtensionsFile(fileContent: String) {
    val file = aspectsPath.resolve("extensions.bzl")
    file.writeText(fileContent)
  }
}
