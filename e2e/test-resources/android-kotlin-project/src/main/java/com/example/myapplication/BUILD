load("@rules_kotlin//kotlin:android.bzl", "kt_android_library")

package(
    default_visibility = ["//src:__subpackages__"],
)

kt_android_library(
    name = "lib",
    srcs = ["MainActivity.kt"],
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    deps = ["@maven//:androidx_appcompat_appcompat"],
)
