package org.jetbrains.bsp.bazel.server.sync.languages.kotlin

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule

data class KotlinModule(
    val languageVersion: String,
    val apiVersion: String,
    val kotlincOptions: KotlincOpts?,
    val associates: List<BuildTargetIdentifier>,
    val javaModule: JavaModule?
) : LanguageData

data class KotlincOpts(
    val includeStdlibs: String,
    val javaParameters: Boolean,
    val jvmTarget: String,
    val warn : String,
    val xAllowResultReturnType : Boolean,
    val xBackendThreads: Int,
    val xEmitJvmTypeAnnotations: Boolean,
    val xEnableIncrementalCompilation: Boolean,
    val xExplicitApiMode: String,
    val xInlineClasses: Boolean,
    val xJvmDefault: String,
    val xLambdas: String,
    val xMultiPlatform: Boolean,
    val xNoCallAssertions: Boolean,
    val xNoOptimize: Boolean,
    val xNoOptimizedCallableReferences: Boolean,
    val xNoParamAssertions: Boolean,
    val xNoReceiverAssertions: Boolean,
    val xOptinList: List<String>,
    val xReportPerf: Boolean,
    val xSamConversions: String,
    val xSkipPrereleaseCheck: Boolean,
    val xUseFirLt: Boolean,
    val xUseK2: Boolean,
)