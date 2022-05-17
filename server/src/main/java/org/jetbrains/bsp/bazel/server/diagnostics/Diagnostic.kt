package org.jetbrains.bsp.bazel.server.diagnostics

data class Diagnostic(
    val position: Position,
    val message: String,
    val level: Level,
    val fileLocation: String,
    val targetLabel: String
)

data class Position(val line: Int, val character: Int)

enum class Level { Error, Warning }
