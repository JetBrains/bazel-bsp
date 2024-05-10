package org.jetbrains.bsp.bazel.server.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

@JvmInline
value class Label private constructor(val value: String) {

  fun targetName(): String =
    value.substringAfterLast(":", "")

  companion object {
    fun parse(value: String): Label =
      Label(value.intern())
  }
}


class LabelSerializer : StdSerializer<Label>(Label::class.java) {
  override fun serialize(value: Label, gen: JsonGenerator, provider: SerializerProvider) {
    gen.writeString(value.value)
  }
}

class LabelKeyDeserializer : KeyDeserializer() {
  override fun deserializeKey(key: String, ctxt: DeserializationContext): Label {
    return Label.parse(key)
  }
}