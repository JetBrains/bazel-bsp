package org.jetbrains.bsp.bazel.server.bsp.utils

import org.jetbrains.bsp.bazel.commons.Constants
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo

class InternalAspectsResolver(bspInfo: BspInfo) {
    private val bspInfo: BspInfo
    private val prefix: Lazy<String> = lazy { getPrefix() }

    init {
        this.bspInfo = bspInfo
    }

    fun resolveLabel(aspect: String): String {
        return prefix.value + aspect
    }

    val bazelBspRoot: String
        get() = bspInfo.bazelBspDir().toString()

    private fun getPrefix(): String {
        return "@" + Constants.ASPECT_REPOSITORY + "//aspects:core.bzl%"
    }
}
