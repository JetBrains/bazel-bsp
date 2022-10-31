package org.jetbrains.bsp.bazel.server.sync.model

enum class Language(
    val id: String,
    val extensions: Set<String>,
    val binary_targets: Set<String> = hashSetOf(),
    dependentNames: Set<String> = hashSetOf(),
) {
    SCALA("scala", hashSetOf(".scala")),
    JAVA("java", hashSetOf(".java"), binary_targets = setOf("java_binary")),
    KOTLIN("kotlin", hashSetOf(".kt"), setOf("kt_jvm_binary"), hashSetOf(JAVA.id)),
    CPP("cpp", hashSetOf(".C", ".cc", ".cpp", ".CPP", ".c++", ".cp", "cxx", ".h", ".hpp")),
    THRIFT("thrift", hashSetOf(".thrift"));

    val allNames: Set<String> = dependentNames + id

    companion object {
        private val ALL = values().toSet()
        fun all() = ALL
    }
}
