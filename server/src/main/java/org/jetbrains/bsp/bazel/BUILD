load("@rules_java//java:defs.bzl", "java_binary")
load("@bazel_sonatype//:defs.bzl", "sonatype_java_export")

sonatype_java_export(
    name = "bsp",
    srcs = glob(["*.java"]),
    maven_coordinates = "org.jetbrains.bsp:bazel-bsp:2.7.2",
    maven_profile = "org.jetbrains",
    pom_template = "//:pom.xml",
    resources = ["//log4j_config"],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//commons",
        "//install",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server",
    ],
)

java_binary(
    name = "bsp-install",
    srcs = glob(["*.java"]),
    main_class = "org.jetbrains.bsp.bazel.install.Install",
    resources = ["//log4j_config"],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//commons",
        "//install",
        "//server/src/main/java/org/jetbrains/bsp/bazel/server",
    ],
)
