package org.jetbrains.bsp.bazel.server.bep

import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.TestSuiteTestFinishData
import org.jetbrains.bsp.TestCaseTestFinishData
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import ch.epfl.scala.bsp4j.TaskId
import ch.epfl.scala.bsp4j.TestStatus
import java.io.File
import java.net.URI
import java.util.UUID

@JacksonXmlRootElement(localName = "testsuites")
data class TestSuites(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "testsuite")
    val testsuite: List<TestSuite> = emptyList()
)

data class TestSuite(
    @JacksonXmlProperty(isAttribute = true)
    val name: String,
    @JacksonXmlProperty(isAttribute = true)
    val timestamp: String,
    @JacksonXmlProperty(isAttribute = true)
    val hostname: String,
    @JacksonXmlProperty(isAttribute = true)
    val tests: Int,
    @JacksonXmlProperty(isAttribute = true)
    val failures: Int,
    @JacksonXmlProperty(isAttribute = true)
    val errors: Int,
    @JacksonXmlProperty(isAttribute = true)
    val time: Double,
    @JacksonXmlProperty(isAttribute = true)
    val id: Int,

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "testcase")
    val testcase: List<TestCase> = emptyList(),

    @JacksonXmlProperty(isAttribute = true, localName = "package")
    val pkg: String?,
    val properties: Any? = null,
    val systemOut: Any? = null,
    val systemErr: Any? = null
)

data class TestCase(
    @JacksonXmlProperty(isAttribute = true)
    val name: String,

    @JacksonXmlProperty(isAttribute = true)
    val classname: String,

    @JacksonXmlProperty(isAttribute = true)
    val time: Double,

    @JacksonXmlProperty(localName = "error")
    val error: TestResultDetail? = null,

    @JacksonXmlProperty(localName = "failure")
    val failure: TestResultDetail? = null,

    @JacksonXmlProperty(localName = "skipped")
    val skipped: TestResultDetail? = null
)

// This is a regular class due to limitations deserializing plain xml tag contents.
// https://github.com/FasterXML/jackson-dataformat-xml/issues/615
class TestResultDetail() {
    @JacksonXmlProperty(isAttribute = true)
    var message: String? = null

    @JacksonXmlProperty(isAttribute = true)
    var type: String?  = null

    @JacksonXmlText(value = true)
    @JacksonXmlProperty(localName = "")
    var content: String?  = null
}

class TestXmlParser(private var parentId: TaskId, private var bspClientTestNotifier: BspClientTestNotifier) {
    /**
     * Processes a test result xml file, reporting suite and test case results as task start and finish notifications.
     * Parent-child relationship is identified within each suite based on the TaskId.
     * @param testXmlUri Uri corresponding to the test result xml file to be processed.
     */
    fun parseAndReport(testXmlUri: String) {
        val testSuites = parseTestXml(testXmlUri)
        testSuites.testsuite.forEach { suite ->
            processSuite(suite)
        }
    }

    private fun parseTestXml(uri: String): TestSuites {
        val xmlMapper = XmlMapper().apply {
            registerKotlinModule()
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        val fileUri = URI.create(uri)
        val testSuites: TestSuites = xmlMapper.readValue(File(fileUri), TestSuites::class.java)
        return testSuites
    }

    private fun processSuite(suite: TestSuite) {
        val suiteTaskId = TaskId(UUID.randomUUID().toString())
        suiteTaskId.parents = listOf(parentId.id)

        val suiteData = TestSuiteTestFinishData(suite.name, suite.pkg, null, null)
        val suiteStatus = when {
            suite.failures > 0 -> TestStatus.FAILED
            suite.errors > 0 -> TestStatus.FAILED
            else -> TestStatus.PASSED
        }

        bspClientTestNotifier.startTest(suite.name, suiteTaskId)
        suite.testcase.forEach { case ->
            processTestCase(suiteTaskId.id, case)
        }
        bspClientTestNotifier.finishTest(suite.name, suiteTaskId, suiteStatus, suite.systemOut.toString(), TestSuiteTestFinishData.DATA_KIND, suiteData)
    }

    private fun processTestCase(parentId: String, case: TestCase) {
        val testCaseTaskId = TaskId(UUID.randomUUID().toString())
        testCaseTaskId.parents = listOf(parentId)

        val outcomeMessage = when {
            case.error != null -> case.error.message
            case.failure != null -> case.failure.message
            case.skipped != null -> case.skipped.message
            else -> null
        }

        val fullOutput = when {
            case.error != null -> case.error.content
            case.failure != null -> case.failure.content
            case.skipped != null -> case.skipped.content
            else -> ""
        }

        val testStatusOutcome = when {
            case.error != null -> TestStatus.FAILED
            case.failure != null -> TestStatus.FAILED
            case.skipped != null -> TestStatus.SKIPPED
            else -> TestStatus.PASSED
        }

        val errorType = when {
            case.error != null -> case.error.type
            case.failure != null -> case.failure.type
            else -> null
        }

        val testCaseData = TestCaseTestFinishData(case.name, case.classname, case.time, fullOutput, errorType)
        bspClientTestNotifier.startTest(case.name, testCaseTaskId)
        bspClientTestNotifier.finishTest(case.name, testCaseTaskId, testStatusOutcome, outcomeMessage, TestCaseTestFinishData.DATA_KIND, testCaseData)
    }
}