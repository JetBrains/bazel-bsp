package org.jetbrains.bsp.bazel.server.bsp.utils

import org.assertj.core.api.Assertions
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser.getSourcesRoot
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.util.stream.Stream

class SourceRootGuesserTest {
    @MethodSource("data")
    @ParameterizedTest(name = "{index}: SourceRootGuesser.getSourcesRoot({0}) should equals {1}")
    fun `should guess source roots`(input: String?, expectedOutput: String?) {
        // when
        val output = getSourcesRoot(URI.create(input))

        // then
        Assertions.assertThat(output).isEqualTo(expectedOutput)
    }

    companion object {
        @JvmStatic
        fun data(): Stream<Arguments> {
            return Stream.of(
                Arguments.of( // given
                    "file:///WORKSPACE_ROOT/java_hello/src/main/java/com/hello/Hello.java",  // then
                    "/WORKSPACE_ROOT/java_hello/src/main/java"
                ),
                Arguments.of( // given
                    "file:///WORKSPACE_ROOT/server/src/test/java/org/jetbrains/bsp/bazel/server/bsp/utils/SourceRootGuesserTest.java",  // then
                    "/WORKSPACE_ROOT/server/src/test/java"
                ),
                Arguments.of( // given
                    "file:///WORKSPACE_ROOT/src/main/java/org/test/java",  // then
                    "/WORKSPACE_ROOT/src/main/java/org/test/java"
                ),
                Arguments.of( // given
                    "file:///WORKSPACE_ROOT/foo/bar",  // then
                    "/WORKSPACE_ROOT/foo"
                )
            )
        }
    }
}
