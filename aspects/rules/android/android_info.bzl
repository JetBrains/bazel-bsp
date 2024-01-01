load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "file_location")

ANDROID_SDK_TOOLCHAIN_TYPE = "@bazel_tools//tools/android:sdk_toolchain_type"

def extract_android_sdk_info(target, ctx, dep_targets, **kwargs):
    if ANDROID_SDK_TOOLCHAIN_TYPE not in ctx.toolchains:
        return None, None
    android_sdk_toolchain = ctx.toolchains[ANDROID_SDK_TOOLCHAIN_TYPE]

    if android_sdk_toolchain == None:
        return None, None
    android_sdk_info = android_sdk_toolchain.android_sdk_info

    android_jar = android_sdk_info.android_jar
    if android_jar == None:
        return None, None

    android_sdk_info_proto = create_struct(
        android_jar = file_location(android_jar),
    )

    return create_proto(target, ctx, android_sdk_info_proto, "android_sdk_info"), None
