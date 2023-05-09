package org.jetbrains.bsp.bazel.commons

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration

private fun getData() =
    listOf(
        //           given                        expected
        Arguments.of(Duration.ofMillis(100), "100ms"),
        Arguments.of(Duration.ofMillis(153), "153ms"),
        Arguments.of(Duration.ofMillis(1000), "1s"),
        Arguments.of(Duration.ofMillis(1500), "1.5s"),
        Arguments.of(Duration.ofMillis(1502), "1.5s"),
        Arguments.of(Duration.ofMillis(1552), "1.6s"),
        Arguments.of(Duration.ofMinutes(3), "3m"),
        Arguments.of(Duration.ofSeconds(90), "1m 30s"),
        Arguments.of(Duration.ofMillis((90 * 1000 + 501).toLong()), "1m 31s")
    )

class FormatTest {

    @ParameterizedTest(name = "Format.duration({0}) should be {1}")
    @MethodSource("org.jetbrains.bsp.bazel.commons.FormatTestKt#getData")
    fun `should format duration properly`(duration: Duration, expectedFormattedDuration: String) {
        // then
        Format.duration(duration) shouldBe expectedFormattedDuration
    }
}
