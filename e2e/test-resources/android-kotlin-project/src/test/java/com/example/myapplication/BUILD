load("@rules_kotlin//kotlin:android.bzl", "kt_android_local_test")

kt_android_local_test(
    name = "lib_test",
    srcs = ["MainActivityTest.kt"],
    manifest = "AndroidManifest.xml",
    test_class = "com.example.myapplication.MainActivityTest",
    deps = [
        "//src/main/java/com/example/myapplication:lib",
        "@maven//:junit_junit",
        "@maven//:org_robolectric_robolectric",
        "@rules_robolectric//bazel:android-all",
    ],
)
