package org.jetbrains.bsp.bazel.server.sync.model

enum class Language(
    val id: String,
    val extensions: Set<String>,
    dependentNames: Set<String> = hashSetOf()
) {
    SCALA("scala", hashSetOf(".scala")),
    JAVA("java", hashSetOf(".java")),
    KOTLIN("kotlin", hashSetOf(".kt"), hashSetOf(JAVA.id)),
    CPP("cpp", hashSetOf(".C", ".cc", ".cpp", ".CPP", ".c++", ".cp", "cxx", ".h", ".hpp")),
    THRIFT("thrift", hashSetOf(".thrift"));

    val allNames: Set<String> = dependentNames + id

    companion object {
        private val ALL = values().toSet()
        fun all() = ALL
    }
}
