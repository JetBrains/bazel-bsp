load("@rules_java//java:defs.bzl", "java_binary")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")

java_binary(
    name = "BazelBspSampleRepoTest",
    srcs = ["BazelBspSampleRepoTest.java"],
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)

java_binary(
    name = "BazelBspActionGraphV1Test",
    srcs = ["BazelBspActionGraphV1Test.java"],
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)

java_binary(
    name = "BazelBspActionGraphV2Test",
    srcs = ["BazelBspActionGraphV2Test.java"],
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//e2e/src/main/java/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)

kt_jvm_binary(
    name = "BazelBspLocalJdkTest",
    srcs = ["BazelBspLocalJdkTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspLocalJdkTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)

kt_jvm_binary(
    name = "BazelBspRemoteJdkTest",
    srcs = ["BazelBspRemoteJdkTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspRemoteJdkTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)

java_binary(
    name = "BazelBspCppProjectTest",
    srcs = ["BazelBspCppProjectTest.java"],
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/java/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
        "@maven//:com_google_guava_guava",
    ],
)
