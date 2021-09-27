load("@rules_cc//cc:defs.bzl", "cc_binary")

cc_binary(
    name = "example",
    srcs = ["main.cpp"],
    copts = [
        "-Iexternal/gtest/include",
    ],
    defines = [
        "BOOST_FALLTHROUGH",
    ],
    linkopts = [
        "-pthread",
    ],
    deps = [
        "@com_google_googletest//:gtest_main",
    ],
)
