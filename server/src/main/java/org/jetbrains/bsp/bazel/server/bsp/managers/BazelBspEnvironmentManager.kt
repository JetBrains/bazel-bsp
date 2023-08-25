package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import java.nio.file.Paths
import kotlin.io.path.writeText

enum class Language(private val fileName: String, val functions: List<String>) {
    Java(
        "//aspects:rules/java/java_info.bzl",
        listOf("extract_java_info", "extract_java_toolchain", "extract_java_runtime")
    ),
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
        val ruleNames = bazelExternalRulesQuery.fetchExternalRuleNames(cancelChecker)
        val languages = getProjectLanguages(ruleNames)
        val fileContent = prepareFileContent(languages)

        createNewExtensionsFile(fileContent)
    }

    private fun getProjectLanguages(allRuleNames: List<String>): List<Language> {
        fun checkForLanguage(languageRuleNames: List<String>, language: Language) =
            checkForLanguageRules(allRuleNames, languageRuleNames, language)

        return listOfNotNull(
            checkForLanguage(listOf("rules_python"), Language.Python),
            checkForLanguage(listOf("rules_cc"), Language.Cpp),
            checkForLanguage(listOf("io_bazel_rules_kotlin"), Language.Kotlin),
            checkForLanguage(listOf("io_bazel_rules_scala"), Language.Scala),
            checkForLanguage(listOf("rules_java"), Language.Java)
        )
    }

    private fun checkForLanguageRules(
        allRuleNames: List<String>,
        languageRuleNames: List<String>,
        language: Language
    ): Language? =
        if (languageRuleNames.any { allRuleNames.contains(it) })
            language
        else null

    private fun prepareFileContent(languages: List<Language>) =
        listOf(
            "# This is a generated file, do not edit it",
            createLoadStatementsString(languages),
            createExtensionListString(languages)
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

    private fun createNewExtensionsFile(fileContent: String) {
        val aspectsPath = Paths.get(internalAspectsResolver.bazelBspRoot, Constants.ASPECTS_ROOT)
        val file = aspectsPath.resolve("extensions.bzl")
        file.writeText(fileContent)
    }
}
