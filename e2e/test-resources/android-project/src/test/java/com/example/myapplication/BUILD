android_local_test(
    name = "lib_test",
    srcs = ["MainActivityTest.java"],
    manifest = "AndroidManifest.xml",
    test_class = "com.example.myapplication.MainActivityTest",
    deps = [
        "//src/main/java/com/example/myapplication:lib",
        "@maven//:junit_junit",
        "@maven//:org_robolectric_robolectric",
        "@maven//:org_robolectric_shadows_framework",
        "@rules_robolectric//bazel:android-all",
    ],
)
