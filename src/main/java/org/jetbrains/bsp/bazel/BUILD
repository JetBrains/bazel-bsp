load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bsp",
    tags = ["maven_coordinates=org.jetbrains.bsp:bazel-bsp:{pom_version}"],
    visibility = ["//visibility:public"],
    resources = ["//src/main/resources:bsp-main-resources"],
    runtime_deps = [
        "//src/main/java/org/jetbrains/bsp/bazel/commons",
        "//src/main/java/org/jetbrains/bsp/bazel/install",
        "//src/main/java/org/jetbrains/bsp/bazel/server",
    ],
)
