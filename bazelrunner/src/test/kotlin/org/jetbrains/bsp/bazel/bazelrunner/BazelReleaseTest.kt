package org.jetbrains.bsp.bazel.bazelrunner

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BazelReleaseTest {

  @Test
  fun `should handle old bazel`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 4.0.0")

    // then
    release.major shouldBe 4
    release.mainRepositoryReferencePrefix() shouldBe "//"
  }

  @Test
  fun `should handle new bazel`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 6.0.0")

    // then
    release.major shouldBe 6
    release.mainRepositoryReferencePrefix() shouldBe "@//"
  }

  @Test
  fun `should handle new bazel unofficial`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 6.0.0-pre20230102")

    // then
    release.major shouldBe 6
  }

  @Test
  fun `should handle new bazel multi-digit version`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 16.0.0")

    // then
    release.major shouldBe 16
  }
}
