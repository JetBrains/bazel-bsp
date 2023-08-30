package org.jetbrains.bsp.bazel.server.bsp.managers

import io.kotest.matchers.equals.shouldBeEqual
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory

class BazelBspEnvironmentManagerTest {

    internal class BspInfoMock(private val dotBazelBspPath: Path) : BspInfo() {
        override fun bazelBspDir(): Path = dotBazelBspPath
    }

    internal class BazelExternalRulesQueryMock(private val ruleNames: List<String>) : BazelExternalRulesQuery {
        override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> = ruleNames
    }

    internal class CancelCheckerMock : CancelChecker {
        override fun checkCanceled() {}
    }

    private val emptyFileContent = "EXTENSIONS=[]TOOLCHAINS=[]"
    private val javaFileContent =
        """load("//aspects:rules/java/java_info.bzl","extract_java_toolchain","extract_java_runtime")""" +
            """load("//aspects:rules/jvm/jvm_info.bzl","extract_jvm_info")""" +
            """EXTENSIONS=[extract_java_toolchain,extract_java_runtime,extract_jvm_info]""" +
            """TOOLCHAINS=["@bazel_tools//tools/jdk:runtime_toolchain_type"]"""
    private val nonJvmExtensionsFileContent =
        """load("//aspects:rules/python/python_info.bzl","extract_python_info")""" +
            """load("//aspects:rules/cpp/cpp_info.bzl","extract_cpp_info")""" +
            """EXTENSIONS=[extract_python_info,extract_cpp_info]""" +
            """TOOLCHAINS=[]"""
    private val allExtensionsFileContent =
        """load("//aspects:rules/python/python_info.bzl","extract_python_info")""" +
            """load("//aspects:rules/cpp/cpp_info.bzl","extract_cpp_info")""" +
            """load("//aspects:rules/kt/kt_info.bzl","extract_kotlin_info")""" +
            """load("//aspects:rules/scala/scala_info.bzl","extract_scala_info","extract_scala_toolchain_info")""" +
            """load("//aspects:rules/java/java_info.bzl","extract_java_toolchain","extract_java_runtime")""" +
            """load("//aspects:rules/jvm/jvm_info.bzl","extract_jvm_info")""" +
            """EXTENSIONS=[extract_python_info,extract_cpp_info,extract_kotlin_info,extract_scala_info,extract_scala_toolchain_info,""" +
            """extract_java_toolchain,extract_java_runtime,extract_jvm_info]""" +
            """TOOLCHAINS=["@io_bazel_rules_scala//scala:toolchain_type","@bazel_tools//tools/jdk:runtime_toolchain_type"]"""

    private lateinit var dotBazelBspAspectsPath: Path
    private lateinit var internalAspectsResolverMock: InternalAspectsResolver

    private fun getExtensionsFileContent(): String =
        dotBazelBspAspectsPath.resolve("extensions.bzl").toFile().readLines().filterNot { it.startsWith('#') }
            .joinToString(separator = "")
            .filterNot { it.isWhitespace() }

    @BeforeEach
    fun before() {
        val dotBazelBspPath = createTempDirectory(".bazelbsp")

        dotBazelBspAspectsPath = dotBazelBspPath.resolve("aspects").createDirectory()
        internalAspectsResolverMock = InternalAspectsResolver(BspInfoMock(dotBazelBspPath))
    }

    @Test
    fun `should create the extensions dot bzl file with no imports and no toolchains`() {
        // given
        val bazelExternalRulesQuery = BazelExternalRulesQueryMock(emptyList())
        val bazelBspEnvironmentManager =
            BazelBspEnvironmentManager(internalAspectsResolverMock, bazelExternalRulesQuery)

        // when
        bazelBspEnvironmentManager.generateLanguageExtensions(CancelCheckerMock())

        // then
        val fileContent = getExtensionsFileContent()
        fileContent shouldBeEqual emptyFileContent
    }

    @Test
    fun `should create the extensions dot bzl file with one import and one toolchain (java)`() {
        // given
        val bazelExternalRulesQuery = BazelExternalRulesQueryMock(listOf("rules_java"))
        val bazelBspEnvironmentManager =
            BazelBspEnvironmentManager(internalAspectsResolverMock, bazelExternalRulesQuery)

        // when
        bazelBspEnvironmentManager.generateLanguageExtensions(CancelCheckerMock())

        // then
        val fileContent = getExtensionsFileContent()
        fileContent shouldBeEqual javaFileContent
    }

    @Test
    fun `should create the extensions dot bzl file with non-jvm imports and without any toolchains`() {
        // given
        val bazelExternalRulesQuery = BazelExternalRulesQueryMock(
            listOf(
                "rules_cc",
                "rules_python"
            )
        )
        val bazelBspEnvironmentManager =
            BazelBspEnvironmentManager(internalAspectsResolverMock, bazelExternalRulesQuery)

        // when
        bazelBspEnvironmentManager.generateLanguageExtensions(CancelCheckerMock())

        // then
        val fileContent = getExtensionsFileContent()
        fileContent shouldBeEqual nonJvmExtensionsFileContent
    }

    @Test
    fun `should create the extensions dot bzl file with all possible imports and toolchains`() {
        // given
        val bazelExternalRulesQuery = BazelExternalRulesQueryMock(
            listOf(
                "rules_java",
                "io_bazel_rules_kotlin",
                "io_bazel_rules_scala",
                "rules_cc",
                "rules_python"
            )
        )
        val bazelBspEnvironmentManager =
            BazelBspEnvironmentManager(internalAspectsResolverMock, bazelExternalRulesQuery)

        // when
        bazelBspEnvironmentManager.generateLanguageExtensions(CancelCheckerMock())

        // then
        val fileContent = getExtensionsFileContent()
        fileContent shouldBeEqual allExtensionsFileContent
    }

    @Test
    fun `should create the extensions dot bzl file with all possible imports, ignoring unknown Bazel rules`() {
        // given
        val bazelExternalRulesQuery = BazelExternalRulesQueryMock(
            listOf(
                "rules_java",
                "unknown_rule_1",
                "io_bazel_rules_kotlin",
                "io_bazel_rules_scala",
                "rules_cc",
                "rules_python",
                "unknown_rule_2"
            )
        )
        val bazelBspEnvironmentManager =
            BazelBspEnvironmentManager(internalAspectsResolverMock, bazelExternalRulesQuery)

        // when
        bazelBspEnvironmentManager.generateLanguageExtensions(CancelCheckerMock())

        // then
        val fileContent = getExtensionsFileContent()
        fileContent shouldBeEqual allExtensionsFileContent
    }

    @Test
    fun `should correctly overwrite the extensions dot bzl file`() {
        // given
        val cancelCheckerMock = CancelCheckerMock()
        val emptyBazelBspEnvironmentManager =
            BazelBspEnvironmentManager(internalAspectsResolverMock, BazelExternalRulesQueryMock(emptyList()))
        val allBazelBspEnvironmentManager = BazelBspEnvironmentManager(
            internalAspectsResolverMock, BazelExternalRulesQueryMock(
                listOf(
                    "rules_java",
                    "io_bazel_rules_kotlin",
                    "io_bazel_rules_scala",
                    "rules_cc",
                    "rules_python"
                )
            )
        )

        // when
        allBazelBspEnvironmentManager.generateLanguageExtensions(cancelCheckerMock)
        var fileContent = getExtensionsFileContent()

        // then
        fileContent shouldBeEqual allExtensionsFileContent

        // when
        emptyBazelBspEnvironmentManager.generateLanguageExtensions(cancelCheckerMock)

        // then
        fileContent = getExtensionsFileContent()
        fileContent shouldBeEqual emptyFileContent
    }
}
