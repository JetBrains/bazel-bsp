package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import java.nio.file.Paths
import kotlin.io.path.writeText

enum class Language(private val fileName: String, val functions: List<String>) {
    Java("//aspects:rules/java/java_info.bzl", listOf("extract_java_toolchain", "extract_java_runtime")),
    Jvm("//aspects:rules/jvm/jvm_info.bzl", listOf("extract_jvm_info")),
    Scala("//aspects:rules/scala/scala_info.bzl", listOf("extract_scala_info", "extract_scala_toolchain_info")),
    Cpp("//aspects:rules/cpp/cpp_info.bzl", listOf("extract_cpp_info")),
    Kotlin("//aspects:rules/kt/kt_info.bzl", listOf("extract_kotlin_info")),
    Python("//aspects:rules/python/python_info.bzl", listOf("extract_python_info"));

    fun toLoadStatement(): String =
        this.functions.joinToString(
            prefix = """load("${this.fileName}", """,
            separator = ", ",
            postfix = ")"
        ) { "\"$it\"" }
}

class BazelBspEnvironmentManager(
    private val internalAspectsResolver: InternalAspectsResolver,
    private val bazelExternalRulesQuery: BazelExternalRulesQuery,
) {

    fun generateLanguageExtensions(cancelChecker: CancelChecker) {
        val externalLanguageRuleNames = bazelExternalRulesQuery.fetchExternalRuleNames(cancelChecker)
        val languages = getProjectLanguages(externalLanguageRuleNames)
        val fileContent = prepareFileContent(languages)

        createNewExtensionsFile(fileContent)
    }

    private fun getProjectLanguages(allRuleNames: List<String>): List<Language> {
        fun checkForLanguage(externalLanguageRuleNames: List<String>, language: Language) =
            checkForLanguageRules(allRuleNames, externalLanguageRuleNames, language)

        return listOfNotNull(
            // Bundled in Bazel
            Language.Java,
            Language.Jvm,

            checkForLanguage(listOf("rules_python"), Language.Python),
            checkForLanguage(listOf("rules_cc"), Language.Cpp),
            checkForLanguage(listOf("io_bazel_rules_kotlin"), Language.Kotlin),
            checkForLanguage(listOf("io_bazel_rules_scala"), Language.Scala),
        )
    }

    private fun checkForLanguageRules(
        allRuleNames: List<String>,
        externalLanguageRuleNames: List<String>,
        language: Language
    ): Language? =
        if (externalLanguageRuleNames.any { allRuleNames.contains(it) })
            language
        else null

    private fun prepareFileContent(languages: List<Language>) =
        listOf(
            "# This is a generated file, do not edit it",
            createLoadStatementsString(languages),
            createExtensionListString(languages),
            createToolchainListString(languages)
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

    private fun createToolchainListString(languages: List<Language>): String =
        languages.mapNotNull {
            when (it) {
                Language.Scala -> """"@io_bazel_rules_scala//scala:toolchain_type""""
                Language.Java -> """"@bazel_tools//tools/jdk:runtime_toolchain_type""""
                else -> null
            }
        }
            .joinToString(prefix = "TOOLCHAINS = [\n", postfix = "\n]", separator = ",\n ") { "\t$it" }


    private fun createNewExtensionsFile(fileContent: String) {
        val aspectsPath = Paths.get(internalAspectsResolver.bazelBspRoot, Constants.ASPECTS_ROOT)
        val file = aspectsPath.resolve("extensions.bzl")
        file.writeText(fileContent)
    }
}
