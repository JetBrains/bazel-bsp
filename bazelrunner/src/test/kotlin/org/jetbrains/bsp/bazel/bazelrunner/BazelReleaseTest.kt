package org.jetbrains.bsp.bazel.bazelrunner

import com.google.common.base.Charsets
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

class BazelReleaseTest {

  @Test
  fun `should handle old bazel`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 4.0.0")

    // then
    release?.major shouldBe 4
    release?.mainRepositoryReferencePrefix(false) shouldBe "//"
  }

  @Test
  fun `should handle new bazel`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 6.0.0")

    // then
    release?.major shouldBe 6
    release?.mainRepositoryReferencePrefix(false) shouldBe "@//"
  }

  @Test
  fun `should handle new bazel unofficial`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 6.0.0-pre20230102")

    // then
    release?.major shouldBe 6
  }

  @Test
  fun `should handle new bazel multi-digit version`() {
    // given & when
    val release = BazelRelease.fromReleaseString("release 16.0.0")

    // then
    release?.major shouldBe 16
  }

  @Test
  fun `should fall back to last supported version in case of error`() {
    // given & when
    val release = BazelRelease.fromReleaseString("debug test").orLatestSupported()

    // then
    release.major shouldBe 6
  }

  @Test
  fun `should correctly parse bazelversion`() {
    // given & when
    val path = copyBazelVersionToTmp()
    val release = BazelRelease.fromBazelVersionFile(path.parent)

    // then
    release?.major shouldBe 6
  }

  private fun copyBazelVersionToTmp() : Path {
    val inputStream = BazelReleaseTest::class.java.getResourceAsStream("/.bazelversion")
    val content = inputStream?.bufferedReader(Charsets.UTF_8)?.readText()
    val tempDir = createTempDirectory("workspace").createDirectories()
    val tempFile = tempDir.resolve(".bazelversion")
    content?.let { tempFile.writeText(it) }
    return tempFile
  }
}
