package org.jetbrains.bsp.bazel.server.model

enum class Language(
    val id: String,
    val extensions: Set<String>,
    val binaryTargets: Set<String> = hashSetOf(),
    dependentNames: Set<String> = hashSetOf(),
) {
    SCALA("scala", hashSetOf(".scala")),
    JAVA("java", hashSetOf(".java"), binaryTargets = setOf("java_binary")),
    KOTLIN("kotlin", hashSetOf(".kt"), setOf("kt_jvm_binary"), hashSetOf(JAVA.id)),
    CPP("cpp", hashSetOf(".C", ".cc", ".cpp", ".CPP", ".c++", ".cp", "cxx", ".h", ".hpp")),
    PYTHON("python", hashSetOf(".py")),
    THRIFT("thrift", hashSetOf(".thrift")),
    RUST("rust", hashSetOf(".rs")),
    ANDROID(
      "android",
      emptySet(),
      setOf("android_binary", "android_library", "android_local_test"),
      hashSetOf(JAVA.id),
    );

    val allNames: Set<String> = dependentNames + id

    companion object {
        private val ALL = values().toSet()
        fun all() = ALL
    }
}
