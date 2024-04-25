package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.JvmBuildTarget
import com.google.gson.annotations.JsonAdapter
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter

@JsonAdapter(EnumTypeAdapter.Factory::class)
public enum class AndroidTargetType(public val value: Int) {
  APP(1),
  LIBRARY(2),
  TEST(3),
}

public data class AndroidBuildTarget(
  val androidJar: String,
  val androidTargetType: AndroidTargetType,
  val manifest: String?,
  val resourceDirectories: List<String>,
  val resourceJavaPackage: String?,
  val assetsDirectories: List<String>,
  var jvmBuildTarget: JvmBuildTarget? = null,
  var kotlinBuildTarget: KotlinBuildTarget? = null,
)
