load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")

kt_jvm_binary(
    name = "BazelBspSampleRepoTest",
    srcs = ["BazelBspSampleRepoTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspSampleRepoTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
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
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
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
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspCppProjectTest",
    srcs = ["BazelBspCppProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspCppProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspPythonProjectTest",
    srcs = ["BazelBspPythonProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspPythonProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspScalaProjectTest",
    srcs = ["BazelBspScalaProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspScalaProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspKotlinProjectTest",
    srcs = ["BazelBspKotlinProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspKotlinProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "//protocol",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "ServerDownloadsBazeliskTest",
    srcs = ["ServerDownloadsBazeliskTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.ServerDownloadsBazeliskTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspRustProjectTest",
    srcs = ["BazelBspRustProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspRustProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspAndroidProjectTest",
    srcs = ["BazelBspAndroidProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspAndroidProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/android",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "//protocol",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspAndroidKotlinProjectTest",
    srcs = ["BazelBspAndroidKotlinProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspAndroidKotlinProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/android",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "//protocol",
        "@maven//:ch_epfl_scala_bsp4j",
    ],
)

kt_jvm_binary(
    name = "BazelBspGoProjectTest",
    srcs = ["BazelBspGoProjectTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspGoProjectTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:ch_epfl_scala_bsp4j",
        "//protocol",
    ],
)
