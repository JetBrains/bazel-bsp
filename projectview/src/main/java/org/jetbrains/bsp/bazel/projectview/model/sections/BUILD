load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "sections",
    srcs = glob(["*.java"]),
    visibility = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview:__subpackages__",
        "//projectview/src/test/java/org/jetbrains/bsp/bazel/projectview/model/sections:__pkg__",
    ],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_commons_commons_collections4",
    ],
)
