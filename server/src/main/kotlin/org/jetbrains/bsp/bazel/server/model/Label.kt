package org.jetbrains.bsp.bazel.server.model

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

@JvmInline
value class Label private constructor(@JsonValue val value: String) {

  fun targetName(): String =
    value.substringAfterLast(":", "")

  override fun toString(): String = value

  companion object {
    fun parse(value: String): Label =
      Label(value.intern())
  }
}

class LabelKeyDeserializer : KeyDeserializer() {
  override fun deserializeKey(key: String, ctxt: DeserializationContext): Label {
    return Label.parse(key)
  }
}
