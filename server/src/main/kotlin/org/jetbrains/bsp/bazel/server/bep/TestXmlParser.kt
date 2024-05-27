package org.jetbrains.bsp.bazel.server.bep

import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.JUnitStyleTestCaseData
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
    // Individual test cases are grouped within a suite.
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
    // Name of the test case, typically method name.
    @JacksonXmlProperty(isAttribute = true)
    val name: String,

    // Class name corresponding to the test case.
    @JacksonXmlProperty(isAttribute = true)
    val classname: String,

    // Time value included with the test case.
    @JacksonXmlProperty(isAttribute = true)
    val time: Double,

    // One of the following will be included if test did not pass.
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
    // Shortened error message, as provided by the test framework.
    @JacksonXmlProperty(isAttribute = true)
    var message: String? = null

    // Error type information.
    // This typically gives the class name of the error, but may be absent or used for a similar alternative value.
    @JacksonXmlProperty(isAttribute = true)
    var type: String? = null

    // Content between the tags, which typically includes the full error stack trace.
    @JacksonXmlText(value = true)
    @JacksonXmlProperty(localName = "")
    var content: String? = null
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

    /**
     * Deserialize the given test report into the TestSuites type as defined above.
     */
    private fun parseTestXml(uri: String): TestSuites {
        val xmlMapper = XmlMapper().apply {
            registerKotlinModule()
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        var rawContent = File(URI.create(uri)).readText()
        // Single empty tag does not deserialize properly, replace with empty pair.
        rawContent = rawContent.replace("<skipped />", "<skipped></skipped>")
        val testSuites: TestSuites = xmlMapper.readValue(rawContent, TestSuites::class.java)
        return testSuites
    }

    /**
     * Convert each TestSuite into a series of taskStart and taskFinish notification to the client.
     * The parents field in each notification's TaskId will be used to indicate the parent-child relationship.
     * @param suite TestSuite to be processed.
     */
    private fun processSuite(suite: TestSuite) {
        val suiteTaskId = TaskId(UUID.randomUUID().toString())
        suiteTaskId.parents = listOf(parentId.id)

        val suiteData = JUnitStyleTestCaseData(suite.time, null, suite.pkg, suite.systemErr.toString(), null)
        val suiteStatus = when {
            suite.failures > 0 -> TestStatus.FAILED
            suite.errors > 0 -> TestStatus.FAILED
            else -> TestStatus.PASSED
        }

        bspClientTestNotifier.startTest(suite.name, suiteTaskId)
        suite.testcase.forEach { case ->
            processTestCase(suite, suiteTaskId.id, case)
        }
        bspClientTestNotifier.finishTest(suite.name, suiteTaskId, suiteStatus, suite.systemOut.toString(), JUnitStyleTestCaseData.DATA_KIND, suiteData)
    }

    /**
     * Convert a TestCase into a taskStart and taskFinish notification to the client.
     * The test case will be associated with its parent suite.
     * @param parentSuite TestSuite to which this test case belongs.
     * @param parentId String identifying the parent's TaskId. Used to indicate the proper tree structure.
     * @param testCase TestCase to be processed and sent to the client.
     */
    private fun processTestCase(parentSuite: TestSuite, parentId: String, testCase: TestCase) {
        val testCaseTaskId = TaskId(UUID.randomUUID().toString())
        testCaseTaskId.parents = listOf(parentId)

        // Extract the error summary message.
        val outcomeMessage = when {
            testCase.error != null -> testCase.error.message
            testCase.failure != null -> testCase.failure.message
            testCase.skipped != null -> testCase.skipped.message
            else -> null
        }

        // Extract the full error message content.
        val fullOutput = when {
            testCase.error != null -> testCase.error.content
            testCase.failure != null -> testCase.failure.content
            testCase.skipped != null -> testCase.skipped.content
            else -> ""
        }

        // Map the outcome into a TestStatus value.
        val testStatusOutcome = when {
            testCase.error != null -> TestStatus.FAILED
            testCase.failure != null -> TestStatus.FAILED
            testCase.skipped != null -> TestStatus.SKIPPED
            else -> TestStatus.PASSED
        }

        // Extract error type information if provided.
        val errorType = when {
            testCase.error != null -> testCase.error.type
            testCase.failure != null -> testCase.failure.type
            else -> null
        }
        val testCaseData = JUnitStyleTestCaseData(testCase.time, testCase.classname, parentSuite.pkg, fullOutput, errorType)
        bspClientTestNotifier.startTest(testCase.name, testCaseTaskId)
        bspClientTestNotifier.finishTest(testCase.name, testCaseTaskId, testStatusOutcome, outcomeMessage, JUnitStyleTestCaseData.DATA_KIND, testCaseData)
    }
}
