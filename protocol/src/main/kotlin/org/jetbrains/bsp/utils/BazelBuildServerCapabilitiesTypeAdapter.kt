package org.jetbrains.bsp.utils

import ch.epfl.scala.bsp4j.BuildServerCapabilities
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.jetbrains.bsp.BazelBuildServerCapabilities

public class BazelBuildServerCapabilitiesTypeAdapter : TypeAdapter<BuildServerCapabilities>() {
  private companion object {
    private val gson = Gson()
  }

  override fun write(writer: JsonWriter, capabilities: BuildServerCapabilities) {
    if (capabilities is BazelBuildServerCapabilities) {
      gson.toJson(capabilities, BazelBuildServerCapabilities::class.java, writer)
    } else {
      gson.toJson(capabilities, BuildServerCapabilities::class.java, writer)
    }
  }

  override fun read(reader: JsonReader): BazelBuildServerCapabilities =
    gson.fromJson(reader, BazelBuildServerCapabilities::class.java)
}
