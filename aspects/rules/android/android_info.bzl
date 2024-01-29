load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "file_location")

ANDROID_SDK_TOOLCHAIN_TYPE = "@bazel_tools//tools/android:sdk_toolchain_type"

def extract_android_info(target, ctx, dep_targets, **kwargs):
    if ANDROID_SDK_TOOLCHAIN_TYPE not in ctx.toolchains:
        return None, None
    android_sdk_toolchain = ctx.toolchains[ANDROID_SDK_TOOLCHAIN_TYPE]

    if android_sdk_toolchain == None:
        return None, None
    android_sdk_info = android_sdk_toolchain.android_sdk_info
    android_jar = file_location(android_sdk_info.android_jar)
    if android_jar == None:
        return None, None

    manifest = None
    if AndroidIdeInfo in target:
        android_ide_info = target[AndroidIdeInfo]
        manifest = file_location(target[AndroidIdeInfo].manifest)

    resources = []
    if hasattr(ctx.rule.attr, "resource_files"):
        for resource in ctx.rule.attr.resource_files:
            for resource_file in resource.files.to_list():
                resources.append(file_location(resource_file))

    kotlin_target_id = None
    if ctx.rule.kind == "android_library" and str(target.label).endswith("_base") and not ctx.rule.attr.srcs:
        # This is a hack to detect the android_library target produced by kt_android_library.
        # It creates an android_library target that ends with _base and a kt_jvm_library target that ends with _kt.
        # Read more here: https://github.com/bazelbuild/rules_kotlin/blob/master/kotlin/internal/jvm/android.bzl
        kotlin_target_id = str(target.label)[:-5] + "_kt"

    android_target_info_proto = create_struct(
        android_jar = android_jar,
        manifest = manifest,
        resources = resources,
        kotlin_target_id = kotlin_target_id,
    )

    return create_proto(target, ctx, android_target_info_proto, "android_target_info"), None
