package org.jetbrains.bsp.bazel.server.bsp.utils

import org.jetbrains.bsp.bazel.bazelrunner.BazelRelease
import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo

class InternalAspectsResolver(val bspInfo: BspInfo, val bazelRelease: BazelRelease) {
    private val prefix: Lazy<String> = lazy { getPrefix() }
    fun resolveLabel(aspect: String): String = prefix.value + aspect

    val bazelBspRoot: String
        get() = bspInfo.bazelBspDir().toString()

    private fun getPrefix(): String = when (bazelRelease.major) {
        in 0..5 -> "@" + Constants.ASPECT_REPOSITORY + "//aspects:core.bzl%"
        else -> "@@" + Constants.ASPECT_REPOSITORY + "//aspects:core.bzl%"
    }
}
