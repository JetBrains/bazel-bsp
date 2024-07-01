package org.jetbrains.bsp.bazel.server.bep

import ch.epfl.scala.bsp4j.*
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path


class TestXmlParserTest {
    private class MockBuildClient : BuildClient {
        val taskStartCalls = mutableListOf<TaskStartParams>()
        val taskFinishCalls = mutableListOf<TaskFinishParams>()

        override fun onBuildShowMessage(p0: ShowMessageParams?) {}
        override fun onBuildLogMessage(p0: LogMessageParams?) {}
        override fun onBuildPublishDiagnostics(p0: PublishDiagnosticsParams?) {}
        override fun onBuildTargetDidChange(p0: DidChangeBuildTarget?) {}
        override fun onBuildTaskStart(p0: TaskStartParams?) {
            p0?.let { taskStartCalls.add(it) }
        }
        override fun onBuildTaskProgress(p0: TaskProgressParams?) {}
        override fun onBuildTaskFinish(p0: TaskFinishParams?) {
            p0?.let { taskFinishCalls.add(it) }
        }
        override fun onRunPrintStdout(p0: PrintParams?) {}
        override fun onRunPrintStderr(p0: PrintParams?) {}
    }

    @Test
    fun `pytest, all passing`(@TempDir tempDir: Path) {
        // given
        val samplePassingContents = """
            <?xml version="1.0" encoding="utf-8"?>
            <testsuites>
                <testsuite name="test_suite" errors="0" failures="0" skipped="0" tests="4" time="0.080"
                           timestamp="2024-05-17T19:23:31.616701" hostname="test-host">
                    <testcase classname="com.example.tests.TestClass" name="test_method_1" time="0.015" />
                    <testcase classname="com.example.tests.TestClass" name="test_method_2" time="0.013" />
                    <testcase classname="com.example.tests.TestClass" name="test_method_3" time="0.001" />
                    <testcase classname="com.example.tests.TestClass" name="test_method_4" time="0.012" />
                </testsuite>
            </testsuites>
        """.trimIndent()

        val client = MockBuildClient()
        val notifier = BspClientTestNotifier(client, "sample-origin")
        val parentId = TaskId("sample-task")

        // when
        TestXmlParser(parentId, notifier).parseAndReport(writeTempFile(tempDir, samplePassingContents))

        // then
        client.taskStartCalls.size shouldBe 5
        client.taskFinishCalls.size shouldBe 5

        val expectedNames = listOf(
                "test_suite",
                "test_method_1",
                "test_method_2",
                "test_method_3",
                "test_method_4"
        )

        client.taskFinishCalls.map { (it.data as TestFinish).displayName } shouldContainExactlyInAnyOrder expectedNames
        client.taskFinishCalls.map { (it.data as TestFinish).status shouldBe TestStatus.PASSED }
        client.taskStartCalls.map { it.taskId } shouldContainExactlyInAnyOrder client.taskFinishCalls.map { it.taskId }
    }

    @Test
    fun `pytest, with failures`(@TempDir tempDir: Path) {
        // given
        val samplePassingContents = """
            <?xml version="1.0" encoding="utf-8"?>
            <testsuites>
                    <testsuite name="mysuite" errors="0" failures="2" skipped="0" tests="22" time="0.163"
                            timestamp="2024-05-21T14:16:34.122101" hostname="test-host">
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_config_1" time="0.001" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_config_2" time="0.001" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_config_3" time="0.000" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_config_4" time="0.000" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_config_5" time="0.001" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_config_6" time="0.001"><skipped></skipped></testcase>
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_config_incorrect_format" time="0.001">
                                    <failure
                                            message="AssertionError: assert 'yaml' == 'other'&#10;  - other&#10;  + yaml">def
                                            test_config_incorrect_format():
                                            ""${'"'}Verify data""${'"'}
                                            config = load_test_config('sample')

                                            assert config.get('sample.name') == 'myname'
                                            &gt; assert config.get('sample.format') == 'other'
                                            E AssertionError: assert 'yaml' == 'other'
                                            E - other
                                            E + yaml

                                            sample/core/config/tests/test_config.py:100: AssertionError</failure>
                            </testcase>
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_other_file" time="0.001" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_sample_file" time="0.001" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_other_value" time="0.001">
                                    <failure
                                            message="AssertionError: assert None is True&#10; +  where None = &lt;bound method Configuration.get of &lt;sample.core.config.FileConfiguration object at 0x7f6c802f3730&gt;&gt;('pigs_go_oink')&#10; +    where &lt;bound method Configuration.get of &lt;sample.core.config.FileConfiguration object at 0x7f6c802f3730&gt;&gt; = &lt;sample.core.config.FileConfiguration object at 0x7f6c802f3730&gt;.get">def
                                            test_other_value():
                                            ""${'"'}Test other_value() updates values.""${'"'}
                                            config = load_test_config('sample')

                                            assert config.get('sample') is None
                                            config.value({'sample': 'yes'})

                                            E AssertionError: assert None is True
                                            E + where None = &lt;bound method Configuration.get of
                                            &lt;sample.core.config.FileConfiguration object at
                                            0x7f6c802f3730&gt;&gt;('sample')
                                            E + where &lt;bound method Configuration.get of
                                            &lt;sample.core.config.FileConfiguration object at
                                            0x7f6c802f3730&gt;&gt; = &lt;sample.core.config.FileConfiguration
                                            object at 0x7f6c802f3730&gt;.get
                                            sample/core/config/tests/test_config.py:137: AssertionError</failure>
                            </testcase>
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_sample_3" time="0.001" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_sample_4" time="0.001" />
                            <testcase classname="sample.core.config.tests.test_config"
                                    name="test_sample_5" time="0.001" />
                    </testsuite>
            </testsuites>
        """.trimIndent()

        val client = MockBuildClient()
        val notifier = BspClientTestNotifier(client, "sample-origin")
        val parentId = TaskId("sample-task")

        // when
        TestXmlParser(parentId, notifier).parseAndReport(writeTempFile(tempDir, samplePassingContents))

        // then
        client.taskStartCalls.size shouldBe 14
        client.taskFinishCalls.size shouldBe 14

        val expectedNames = listOf(
                "test_config_1", "test_config_2", "test_config_3", "test_config_4", "test_config_5", "test_config_6", "test_config_incorrect_format", "test_other_file", "test_sample_file", "test_other_value", "test_sample_3", "test_sample_4", "test_sample_5", "mysuite"
        )

        client.taskFinishCalls.map { (it.data as TestFinish).displayName } shouldContainExactlyInAnyOrder expectedNames
        client.taskFinishCalls.map {
            val data = (it.data as TestFinish)
            when (data.displayName) {
                "mysuite" -> {
                    data.status shouldBe TestStatus.FAILED
                }
                "test_other_value", "test_config_incorrect_format" -> {
                    data.status shouldBe TestStatus.FAILED
                    data.message shouldContain "AssertionError"
                }
                "test_config_6" -> {
                    data.status shouldBe TestStatus.SKIPPED
                }
                else -> {
                    data.status shouldBe TestStatus.PASSED
                }
            }
        }
        client.taskStartCalls.map { it.taskId } shouldContainExactlyInAnyOrder client.taskFinishCalls.map { it.taskId }
    }

    @Test
    fun `junit, all passing`(@TempDir tempDir: Path) {
        // given
        val samplePassingContents = """
            <?xml version='1.0' encoding='UTF-8'?>
            <testsuites>
              <testsuite name='com.example.testing.base.Tests' timestamp='2024-05-14T19:23:32.883Z' hostname='localhost' tests='20' failures='0' errors='0' time='13.695' package='' id='0'>
                <properties />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite1' timestamp='2024-05-14T19:23:32.883Z' hostname='localhost' tests='4' failures='0' errors='0' time='0.065' package='' id='1'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite1' time='0.032' />
                <testcase name='test2' classname='com.example.optimization.TestSuite1' time='0.019' />
                <testcase name='test3' classname='com.example.optimization.TestSuite1' time='0.002' />
                <testcase name='test4' classname='com.example.optimization.TestSuite1' time='0.003' />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite2' timestamp='2024-05-14T19:23:32.952Z' hostname='localhost' tests='5' failures='0' errors='0' time='13.528' package='' id='2'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite2' time='3.101' />
                <testcase name='test2' classname='com.example.optimization.TestSuite2' time='4.368' />
                <testcase name='test3' classname='com.example.optimization.TestSuite2' time='1.778' />
                <testcase name='test4' classname='com.example.optimization.TestSuite2' time='1.838' />
                <testcase name='test5' classname='com.example.optimization.TestSuite2' time='2.434' />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite3' timestamp='2024-05-14T19:23:46.484Z' hostname='localhost' tests='1' failures='0' errors='0' time='0.004' package='' id='3'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite3' time='0.004' />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite4' timestamp='2024-05-14T19:23:46.491Z' hostname='localhost' tests='1' failures='0' errors='0' time='0.0' package='' id='4'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite4' time='0.0' />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite5' timestamp='2024-05-14T19:23:46.494Z' hostname='localhost' tests='1' failures='0' errors='0' time='0.0' package='' id='5'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite5' time='0.0' />
                <system-out />
                <system-err />
              </testsuite>
            </testsuites>
        """.trimIndent()

        val client = MockBuildClient()
        val notifier = BspClientTestNotifier(client, "sample-origin")
        val parentId = TaskId("sample-task")

        // when
        TestXmlParser(parentId, notifier).parseAndReport(writeTempFile(tempDir, samplePassingContents))

        // then
        client.taskStartCalls.size shouldBe 18
        client.taskFinishCalls.size shouldBe 18

        val expectedNames = listOf(
                "com.example.testing.base.Tests", "com.example.optimization.TestSuite1", "test1", "test2", "test3", "test4",
                "com.example.optimization.TestSuite2", "test1", "test2", "test3", "test4", "test5",
                "com.example.optimization.TestSuite3", "test1",
                "com.example.optimization.TestSuite4", "test1",
                "com.example.optimization.TestSuite5", "test1",
        )

        client.taskFinishCalls.map { (it.data as TestFinish).displayName } shouldContainExactlyInAnyOrder expectedNames
        client.taskFinishCalls.map { (it.data as TestFinish).status shouldBe TestStatus.PASSED }
        client.taskStartCalls.map { it.taskId } shouldContainExactlyInAnyOrder client.taskFinishCalls.map { it.taskId }
    }

    @Test
    fun `junit, with skip and failures`(@TempDir tempDir: Path) {
        // given
        val samplePassingContents = """
            <?xml version='1.0' encoding='UTF-8'?>
            <testsuites>
              <testsuite name='com.example.testing.base.Tests' timestamp='2024-05-21T14:59:39.108Z' hostname='localhost' tests='20' failures='1' errors='0' time='13.06' package='' id='0'>
                <properties />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite1' timestamp='2024-05-21T14:59:39.108Z' hostname='localhost' tests='4' failures='1' errors='0' time='0.058' package='' id='1'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite1' time='0.027' />
                <testcase name='sampleFailedTest' classname='com.example.optimization.TestSuite1' time='0.021'>
                  <failure message='expected:&lt;0.6946&gt; but was:&lt;0.5946238516952311&gt;' type='java.lang.AssertionError'>java.lang.AssertionError: expected:&lt;0.6946&gt; but was:&lt;0.5946238516952311&gt;
                at org.junit.Assert.fail(Assert.java:88)
                at org.junit.Assert.failNotEquals(Assert.java:834)
                at org.junit.Assert.assertEquals(Assert.java:553)
                at org.junit.Assert.assertEquals(Assert.java:683)
                at com.example.optimization.TestSuite1.test2(TestSuite1.java:141)
                at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
                at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
                at java.base/java.lang.reflect.Method.invoke(Method.java:566)
                at org.junit.runners.model.FrameworkMethod${'$'}1.runReflectiveCall(FrameworkMethod.java:50)
                at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
                at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
                at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
                at com.example.testing.base.mode.TestModeCheckRule1.evaluate(TestModeCheckRule.java:39)
                at org.junit.rules.RunRules.evaluate(RunRules.java:20)
                at org.junit.internal.runners.statements.FailOnTimeoutCallableStatement.call(FailOnTimeout.java:298)
                at org.junit.internal.runners.statements.FailOnTimeoutCallableStatement.call(FailOnTimeout.java:292)
                at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
                at java.base/java.lang.Thread.run(Thread.java:829)
            </failure></testcase>
                <testcase name='test3' classname='com.example.optimization.TestSuite1' time='0.001' />
                <testcase name='test4' classname='com.example.optimization.TestSuite1' time='0.002' />
                <testcase name='sampleSkippedTest' classname='com.example.optimization.TestSuite1' time='0.0' >
                  <skipped />
                </testcase>
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite2' timestamp='2024-05-21T14:59:39.169Z' hostname='localhost' tests='5' failures='0' errors='0' time='12.916' package='' id='2'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite2' time='2.917' />
                <testcase name='test2' classname='com.example.optimization.TestSuite2' time='4.176' />
                <testcase name='test3' classname='com.example.optimization.TestSuite2' time='1.697' />
                <testcase name='test4' classname='com.example.optimization.TestSuite2' time='1.797' />
                <testcase name='test5' classname='com.example.optimization.TestSuite2' time='2.321' />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite3' timestamp='2024-05-21T14:59:52.089Z' hostname='localhost' tests='1' failures='0' errors='0' time='0.003' package='' id='3'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite3' time='0.003' />
                <system-out />
                <system-err />
              </testsuite>
              <testsuite name='com.example.optimization.TestSuite4' timestamp='2024-05-21T14:59:52.095Z' hostname='localhost' tests='1' failures='0' errors='0' time='0.0' package='' id='4'>
                <properties />
                <testcase name='test1' classname='com.example.optimization.TestSuite4' time='0.0' />
                <system-out />
                <system-err />
              </testsuite>
            </testsuites>
        """.trimIndent()

        val client = MockBuildClient()
        val notifier = BspClientTestNotifier(client, "sample-origin")
        val parentId = TaskId("sample-task")

        // when
        TestXmlParser(parentId, notifier).parseAndReport(writeTempFile(tempDir, samplePassingContents))

        // then
        client.taskStartCalls.size shouldBe 17
        client.taskFinishCalls.size shouldBe 17

        val expectedNames = listOf(
                "com.example.testing.base.Tests", "com.example.optimization.TestSuite1", "test1", "test2", "test3", "test4", "test5",
                "com.example.optimization.TestSuite2", "test1", "sampleFailedTest", "test3", "test4", "sampleSkippedTest",
                "com.example.optimization.TestSuite3", "test1",
                "com.example.optimization.TestSuite4", "test1"
        )

        client.taskFinishCalls.map { (it.data as TestFinish).displayName } shouldContainExactlyInAnyOrder expectedNames
        client.taskStartCalls.map { it.taskId } shouldContainExactlyInAnyOrder client.taskFinishCalls.map { it.taskId }
        client.taskFinishCalls.map {
            val data = (it.data as TestFinish)
            when (data.displayName) {
                "com.example.optimization.TestSuite1", "com.example.testing.base.Tests" -> {
                    data.status shouldBe TestStatus.FAILED
                }
                "sampleFailedTest" -> {
                    data.status shouldBe TestStatus.FAILED
                    data.message shouldContain "expected:"
                }
                "sampleSkippedTest" -> {
                    data.status shouldBe TestStatus.SKIPPED
                }
                else -> {
                    data.status shouldBe TestStatus.PASSED
                }
            }
        }
    }

    private fun writeTempFile(tempDir: Path, contents: String): String {
        val tempFile = tempDir.resolve("tempFile.xml").toFile()
        tempFile.writeText(contents)
        return tempFile.toURI().toString()
    }
}
