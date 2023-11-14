package org.jetbrains.bsp.bazel.server.bsp.managers

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StructuredFilesUtilsTests {
  @Nested
  inner class JsonElementTests {
    @Test
    fun `should return null for invalid json string`() {
      // given
      val s = "\\a"

      // when
      val parsedJson = s.toJson()

      // then
      parsedJson shouldBe null
    }

    @Test
    fun `should return non-null for valid json string`() {
      // given
      val s = "{}"

      // when
      val parsedJson = s.toJson()

      // then
      parsedJson shouldNotBe null
    }

    @Test
    fun `should return empty list when extracting empty json`() {
      // given
      val s = "{}"

      // when
      val l = s.toJson().extractValuesFromKey("key")

      // then
      l shouldBe emptyList()
    }

    @Test
    fun `should return empty list when extracting json without valid key`() {
      // given
      val s = """
        {
          "k": "v"
        }
      """.trimIndent()

      // when
      val l = s.toJson().extractValuesFromKey("key")

      // then
      l shouldBe emptyList()
    }

    @Test
    fun `should return non-empty list when extracting json with valid key`() {
      // given
      val s = """
        {
          "key": "v"
        }
      """.trimIndent()

      // when
      val l = s.toJson().extractValuesFromKey("key")

      // then
      l.shouldNotBeEmpty()
    }

    @Test
    fun `should match all elements recursively when extracting json with valid key`() {
      // given
      val s = """
        {
          "key": "val1",
          "a_list": [
            {
              "key": "val2",
              "nested_list": [
                {
                  "key": "val3"
                }
              ]
            }
          ],
          "b_list": [
            {
              "key": "val4"
            }
          ]
        }
      """.trimIndent()

      // when
      val l = s.toJson().extractValuesFromKey("key")

      // then
      l shouldContainExactlyInAnyOrder listOf("val1", "val2", "val3", "val4")
    }
  }
}