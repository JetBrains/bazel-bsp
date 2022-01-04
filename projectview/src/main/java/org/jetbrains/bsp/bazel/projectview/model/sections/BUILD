load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "sections",
    srcs = glob(["*.java"]),
    visibility = ["//projectview:__subpackages__"],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_commons_commons_collections4",
    ],
)
