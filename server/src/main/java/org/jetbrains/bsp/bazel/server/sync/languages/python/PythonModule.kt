package org.jetbrains.bsp.bazel.server.sync.languages.python

import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData

data class PythonModule(
    val interpreter: String,
    val version: String
) : LanguageData


