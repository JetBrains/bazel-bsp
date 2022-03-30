package org.jetbrains.bsp.bazel.server.bep.parsers.error;

import static org.assertj.core.api.Assertions.assertThat;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.Diagnostic;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.Range;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StderrDiagnosticsParserTest {

  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(
            // given - error in BUILD file
            "Loading:\n"
                + "Loading: 0 packages loaded\n"
                + "Analyzing: target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured)\n"
                + "INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured).\n"
                + "INFO: Found 1 target...\n"
                + "[0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt\n"
                + "ERROR: /user/workspace/path/to/package/BUILD:12:37: in java_test rule"
                + " //path/to/package:test: target '//path/to/another/package:lib' is not visible"
                + " from target '//path/to/package:test'. Check the visibility declaration of the"
                + " former target if you think the dependency is legitimate",
            // then
            Map.of(
                "/user/workspace/path/to/package/BUILD",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(12, 37), new Position(12, 37)),
                            "ERROR: /user/workspace/path/to/package/BUILD:12:37: in java_test rule"
                                + " //path/to/package:test: target '//path/to/another/package:lib'"
                                + " is not visible from target '//path/to/package:test'. Check the"
                                + " visibility declaration of the former target if you think the"
                                + " dependency is legitimate") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "/user/workspace/path/to/package/BUILD",
                        new BuildTargetIdentifier("//path/to/package:test"))))),
        Arguments.of(
            // given - error in one source file
            "Loading:\n"
                + "Loading: 0 packages loaded\n"
                + "Analyzing: target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured)\n"
                + "INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured).\n"
                + "INFO: Found 1 target...\n"
                + "[0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt\n"
                + "ERROR: /user/workspace/path/to/package/BUILD:12:37: scala //path/to/package:test"
                + " failed: (Exit 1): scalac failed: error executing command"
                + " bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac"
                + " @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params\n"
                + "path/to/package/Test.scala:3: error: type mismatch;\n"
                + " found   : String(\"test\")\n"
                + " required: Int\n"
                + "  val foo: Int = \"test\"\n"
                + "                 ^\n"
                + "one error found\n"
                + "Build failed\n"
                + "java.lang.RuntimeException: Build failed\n"
                + "\tat io.bazel.rulesscala.scalac.ScalacWorker.compileScalaSources(ScalacWorker.java:280)\n"
                + "\tat io.bazel.rulesscala.scalac.ScalacWorker.work(ScalacWorker.java:63)\n"
                + "\tat io.bazel.rulesscala.worker.Worker.persistentWorkerMain(Worker.java:92)\n"
                + "\tat io.bazel.rulesscala.worker.Worker.workerMain(Worker.java:46)\n"
                + "\tat io.bazel.rulesscala.scalac.ScalacWorker.main(ScalacWorker.java:26)\n"
                + "Target //path/to/package:test failed to build\n"
                + "Use --verbose_failures to see the command lines of failed build steps.\n"
                + "INFO: Elapsed time: 0.220s, Critical Path: 0.09s\n"
                + "INFO: 2 processes: 2 internal.\n"
                + "FAILED: Build did NOT complete successfully",
            // then
            Map.of(
                "/user/workspace/path/to/package/BUILD",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(12, 37), new Position(12, 37)),
                            "ERROR: /user/workspace/path/to/package/BUILD:12:37: scala"
                                + " //path/to/package:test failed: (Exit 1): scalac failed: error"
                                + " executing command"
                                + " bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac"
                                + " @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "/user/workspace/path/to/package/BUILD",
                        new BuildTargetIdentifier("//path/to/package:test"))),
                "path/to/package/Test.scala",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(3, 0), new Position(3, 0)),
                            "path/to/package/Test.scala:3: error: type mismatch;\n"
                                + " found   : String(\"test\")\n"
                                + " required: Int\n"
                                + "  val foo: Int = \"test\"\n"
                                + "                 ^") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "path/to/package/Test.scala",
                        new BuildTargetIdentifier("//path/to/package:test"))))),
        Arguments.of(
            // given - errors in two source files
            "Loading:\n"
                + "Loading: 0 packages loaded\n"
                + "Analyzing: target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured)\n"
                + "INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured).\n"
                + "INFO: Found 1 target...\n"
                + "[0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt\n"
                + "ERROR: /user/workspace/path/to/package/BUILD:12:37: scala //path/to/package:test"
                + " failed (Exit 1): scalac failed: error executing command"
                + " bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac"
                + " @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params\n"
                + "path/to/package/Test1.scala:21: error: type mismatch;\n"
                + "found   : Int(42)\n"
                + "required: String\n"
                + "val x: String = 42\n"
                + "^\n"
                + "path/to/package/Test2.scala:37: error: type mismatch;\n"
                + "found   : String(\"test\")\n"
                + "required: Int\n"
                + "val x: Int = \"test\"\n"
                + "^\n"
                + "2 errors\n"
                + "Build failed\n"
                + "java.lang.RuntimeException: Build failed\n"
                + "at io.bazel.rulesscala.scalac.ScalacWorker.compileScalaSources(ScalacWorker.java:280)\n"
                + "at io.bazel.rulesscala.scalac.ScalacWorker.work(ScalacWorker.java:63)\n"
                + "at io.bazel.rulesscala.worker.Worker.persistentWorkerMain(Worker.java:92)\n"
                + "at io.bazel.rulesscala.worker.Worker.workerMain(Worker.java:46)\n"
                + "at io.bazel.rulesscala.scalac.ScalacWorker.main(ScalacWorker.java:26)\n"
                + "Target //path/to/package:test failed to build\n"
                + "Use --verbose_failures to see the command lines of failed build steps.\n"
                + "INFO: Elapsed time: 0.216s, Critical Path: 0.12s\n"
                + "INFO: 2 processes: 2 internal.\n"
                + "FAILED: Build did NOT complete successfully\n"
                + "FAILED: Build did NOT complete successfully\n"
                + "FAILED: Build did NOT complete successfully",
            // then
            Map.of(
                "/user/workspace/path/to/package/BUILD",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(12, 37), new Position(12, 37)),
                            "ERROR: /user/workspace/path/to/package/BUILD:12:37: scala"
                                + " //path/to/package:test failed (Exit 1): scalac failed: error"
                                + " executing command"
                                + " bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac"
                                + " @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "/user/workspace/path/to/package/BUILD",
                        new BuildTargetIdentifier("//path/to/package:test"))),
                "path/to/package/Test1.scala",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(21, 0), new Position(21, 0)),
                            "path/to/package/Test1.scala:21: error: type mismatch;\n"
                                + "found   : Int(42)\n"
                                + "required: String\n"
                                + "val x: String = 42\n"
                                + "^") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "path/to/package/Test1.scala",
                        new BuildTargetIdentifier("//path/to/package:test"))),
                "path/to/package/Test2.scala",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(37, 0), new Position(37, 0)),
                            "path/to/package/Test2.scala:37: error: type mismatch;\n"
                                + "found   : String(\"test\")\n"
                                + "required: Int\n"
                                + "val x: Int = \"test\"\n"
                                + "^") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "path/to/package/Test2.scala",
                        new BuildTargetIdentifier("//path/to/package:test"))))),
        Arguments.of(
            // given - errors in one source file
            "Loading:\n"
                + "Loading: 0 packages loaded\n"
                + "Analyzing: target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured)\n"
                + "INFO: Analyzed target //path/to/package:test (0 packages loaded, 0 targets"
                + " configured).\n"
                + "INFO: Found 1 target...\n"
                + "[0 / 3] [Prepa] BazelWorkspaceStatusAction stable-status.txt\n"
                + "ERROR: /user/workspace/path/to/package/BUILD:12:37: scala //path/to/package:test"
                + " failed (Exit 1): scalac failed: error executing command"
                + " bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac"
                + " @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params\n"
                + "path/to/package/Test.scala:21: error: type mismatch;\n"
                + "found   : Int(42)\n"
                + "required: String\n"
                + "val x: String = 42\n"
                + "^\n"
                + "path/to/package/Test.scala:37: error: type mismatch;\n"
                + "found   : String(\"test\")\n"
                + "required: Int\n"
                + "val x: Int = \"test\"\n"
                + "^\n"
                + "2 errors\n"
                + "Build failed\n"
                + "java.lang.RuntimeException: Build failed\n"
                + "at io.bazel.rulesscala.scalac.ScalacWorker.compileScalaSources(ScalacWorker.java:280)\n"
                + "at io.bazel.rulesscala.scalac.ScalacWorker.work(ScalacWorker.java:63)\n"
                + "at io.bazel.rulesscala.worker.Worker.persistentWorkerMain(Worker.java:92)\n"
                + "at io.bazel.rulesscala.worker.Worker.workerMain(Worker.java:46)\n"
                + "at io.bazel.rulesscala.scalac.ScalacWorker.main(ScalacWorker.java:26)\n"
                + "Target //path/to/package:test failed to build\n"
                + "Use --verbose_failures to see the command lines of failed build steps.\n"
                + "INFO: Elapsed time: 0.216s, Critical Path: 0.12s\n"
                + "INFO: 2 processes: 2 internal.\n"
                + "FAILED: Build did NOT complete successfully\n"
                + "FAILED: Build did NOT complete successfully\n"
                + "FAILED: Build did NOT complete successfully",
            // then
            Map.of(
                "/user/workspace/path/to/package/BUILD",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(12, 37), new Position(12, 37)),
                            "ERROR: /user/workspace/path/to/package/BUILD:12:37: scala"
                                + " //path/to/package:test failed (Exit 1): scalac failed: error"
                                + " executing command"
                                + " bazel-out/darwin-opt-exec-2B5CBBC6/bin/external/io_bazel_rules_scala/src/java/io/bazel/rulesscala/scalac/scalac"
                                + " @bazel-out/darwin-fastbuild/bin/path/to/package/test.jar-0.params") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "/user/workspace/path/to/package/BUILD",
                        new BuildTargetIdentifier("//path/to/package:test"))),
                "path/to/package/Test.scala",
                List.of(
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(21, 0), new Position(21, 0)),
                            "path/to/package/Test.scala:21: error: type mismatch;\n"
                                + "found   : Int(42)\n"
                                + "required: String\n"
                                + "val x: String = 42\n"
                                + "^") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "path/to/package/Test.scala",
                        new BuildTargetIdentifier("//path/to/package:test")),
                    new FileDiagnostic(
                        new Diagnostic(
                            new Range(new Position(37, 0), new Position(37, 0)),
                            "path/to/package/Test.scala:37: error: type mismatch;\n"
                                + "found   : String(\"test\")\n"
                                + "required: Int\n"
                                + "val x: Int = \"test\"\n"
                                + "^") {
                          {
                            setSeverity(DiagnosticSeverity.ERROR);
                          }
                        },
                        "path/to/package/Test.scala",
                        new BuildTargetIdentifier("//path/to/package:test"))))));
  }

  @MethodSource("data")
  @ParameterizedTest(name = "{index}: StderrDiagnosticsParser.parse({0}) should equals {1}")
  public void shouldParseEntireEventStderr(
      String error, Map<String, List<FileDiagnostic>> expectedDiagnostics) {
    // when
    var diagnostics = StderrDiagnosticsParser.parse(error);

    // then
    assertThat(diagnostics).containsExactlyInAnyOrderEntriesOf(expectedDiagnostics);
  }
}
