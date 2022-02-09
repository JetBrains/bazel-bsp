load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "sections",
    srcs = glob(["*.java"]),
    visibility = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser:__subpackages__",
        "//projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/parser/sections:__pkg__",
    ],
    exports = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
    ],
    deps = [
        "//commons",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/model/sections",
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
