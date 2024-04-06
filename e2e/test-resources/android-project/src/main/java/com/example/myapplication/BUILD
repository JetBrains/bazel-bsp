package(
    default_visibility = ["//src:__subpackages__"],
)

android_library(
    name = "lib",
    srcs = ["MainActivity.java"],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = ["@maven//:androidx_appcompat_appcompat"],
)
