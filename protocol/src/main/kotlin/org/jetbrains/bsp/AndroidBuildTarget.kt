package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.JvmBuildTarget
import com.google.gson.annotations.JsonAdapter
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter
import java.net.URI

@JsonAdapter(EnumTypeAdapter.Factory::class)
public enum class AndroidTargetType(public val value: Int) {
  APP(1),
  LIBRARY(2),
  TEST(3),
}

public data class AndroidBuildTarget(
  val androidJar: URI,
  val androidTargetType: AndroidTargetType,
  val manifest: URI?,
  val resourceFolders: List<URI>,
  var jvmBuildTarget: JvmBuildTarget? = null,
)
