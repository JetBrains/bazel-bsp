package org.jetbrains.bsp.bazel.server.sync.model

enum class Language(
    val id: String,
    val extensions: Set<String>,
    val binaryTargets: Set<String> = hashSetOf(),
    dependentNames: Set<String> = hashSetOf(),
    val dependencyRegex: Regex? = null,
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
      setOf("android_binary", "android_library"),
      hashSetOf(JAVA.id),
      // This should be removed once https://github.com/bazelbuild/rules_kotlin/issues/273 is fixed
      "@@rules_kotlin~.*//third_party:android_sdk".toRegex(),
    );

    val allNames: Set<String> = dependentNames + id

    companion object {
        private val ALL = values().toSet()
        fun all() = ALL
    }
}
