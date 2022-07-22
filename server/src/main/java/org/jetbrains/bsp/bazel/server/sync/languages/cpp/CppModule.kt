package org.jetbrains.bsp.bazel.server.sync.languages.cpp

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData

data class CppModule(
    val copts: List<String>,
    val defines: List<String>,
    val linkOpts: List<String>,
    val linkShared: Boolean) : LanguageData

