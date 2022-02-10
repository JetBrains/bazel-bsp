load("@rules_java//java:defs.bzl", "java_test")

java_test(
    name = "ProjectViewRawSectionsTest",
    size = "small",
    srcs = ["ProjectViewRawSectionsTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
        "@maven//:org_apache_commons_commons_collections4",
    ],
)

java_test(
    name = "ProjectViewRawSectionTest",
    size = "small",
    srcs = ["ProjectViewRawSectionTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
    ],
)

java_test(
    name = "ProjectViewSectionSplitterTest",
    size = "small",
    srcs = ["ProjectViewSectionSplitterTest.java"],
    runtime_deps = [
        "@maven//:junit_junit",
    ],
    deps = [
        "//projectview/src/main/java/org/jetbrains/bsp/bazel/projectview/parser/splitter",
    ],
)
