load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")

kt_jvm_binary(
    name = "BazelBspSampleRepoTest",
    srcs = ["BazelBspSampleRepoTest.kt"],
    main_class = "org.jetbrains.bsp.bazel.BazelBspSampleRepoTest",
    resources = ["//e2e/src/main/resources:bsp-e2e-resources"],
    visibility = ["//e2e:__subpackages__"],
    deps = [
        "//commons",
        "//e2e/src/main/kotlin/org/jetbrains/bsp/bazel/base",
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
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
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
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
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
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
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
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
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
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
        "@maven//:com_github_zpp_This_is_fine_build_server_protocol_bsp4j",
    ],
)