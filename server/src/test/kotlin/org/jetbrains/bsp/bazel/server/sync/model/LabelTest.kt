package org.jetbrains.bsp.bazel.server.sync.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class LabelTest {

  @Test
  fun `should return target name for label with bazel 6 target`() {
    // given
    val label = Label("@//path/to/target:targetName")

    // when
    val targetName = label.targetName()

    // then
    targetName shouldBe "targetName"
  }

  @Test
  fun `should return target name for label with bazel 5 target`() {
    // given
    val label = Label("//path/to/target:targetName")

    // when
    val targetName = label.targetName()

    // then
    targetName shouldBe "targetName"
  }

  @Test
  fun `should return empty string for label with target without target name`() {
    // given
    val label = Label("//path/to/target")

    // when
    val targetName = label.targetName()

    // then
    targetName shouldBe ""
  }
}
