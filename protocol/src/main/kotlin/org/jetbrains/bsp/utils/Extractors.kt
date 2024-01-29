package org.jetbrains.bsp.utils

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetDataKind
import ch.epfl.scala.bsp4j.JvmBuildTarget
import ch.epfl.scala.bsp4j.PythonBuildTarget
import ch.epfl.scala.bsp4j.ScalaBuildTarget
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.jetbrains.bsp.AndroidBuildTarget
import org.jetbrains.bsp.KotlinBuildTarget

private inline fun <reified Data> extractData(target: BuildTarget, kind: String): Data? =
  if (target.dataKind == kind) {
    if (target.data is Data) target.data as Data
    else Gson().fromJson(
      target.data as JsonObject,
      Data::class.java,
    )
  } else null

public fun extractPythonBuildTarget(target: BuildTarget): PythonBuildTarget? =
  extractData(target, BuildTargetDataKind.PYTHON)

public fun extractScalaBuildTarget(target: BuildTarget): ScalaBuildTarget? =
  extractData(target, BuildTargetDataKind.SCALA)

public fun extractAndroidBuildTarget(target: BuildTarget): AndroidBuildTarget? =
  extractData(target, "android")

public fun extractKotlinBuildTarget(target: BuildTarget): KotlinBuildTarget? =
  extractData(target, "kotlin")

public fun extractJvmBuildTarget(target: BuildTarget): JvmBuildTarget? =
  extractData(target, BuildTargetDataKind.JVM)
    ?: extractAndroidBuildTarget(target)?.jvmBuildTarget
    ?: extractKotlinBuildTarget(target)?.jvmBuildTarget
    ?: extractScalaBuildTarget(target)?.jvmBuildTarget
