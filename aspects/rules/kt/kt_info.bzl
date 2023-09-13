load("@io_bazel_rules_kotlin//kotlin/internal:defs.bzl", "KtJvmInfo")
load("@io_bazel_rules_kotlin//kotlin/internal:opts.bzl", "KotlincOptions")
load("//aspects:utils/utils.bzl", "convert_struct_to_dict", "create_proto", "create_struct")

KOTLIN_TOOLCHAIN_TYPE = "@io_bazel_rules_kotlin//kotlin/internal:kt_toolchain_type"

def extract_kotlin_info(target, ctx, **kwargs):
    if KtJvmInfo not in target:
        return None, None

    # Only supports JVM platform now
    provider = target[KtJvmInfo]

    language_version = getattr(provider, "language_version", None)
    api_version = language_version
    associates = getattr(ctx.rule.attr, "associates", [])
    associates_labels = [str(associate.label) for associate in associates]

    kotlin_toolchain = ctx.toolchains[KOTLIN_TOOLCHAIN_TYPE]
    toolchain_kotlinc_opts = kotlin_toolchain.kotlinc_options
    kotlinc_opts_target = getattr(ctx.rule.attr, "kotlinc_opts", None)
    kotlinc_opts = kotlinc_opts_target[KotlincOptions] if kotlinc_opts_target and KotlincOptions in kotlinc_opts_target else toolchain_kotlinc_opts

    kotlin_info = dict(
        language_version = language_version,
        api_version = api_version,
        associates = associates_labels,
    )

    kotlinc_opts_dict = convert_struct_to_dict(kotlinc_opts)
    if not kotlinc_opts_dict.get("jvm_target"):
        kotlinc_opts_dict["jvm_target"] = getattr(kotlin_toolchain, "jvm_target", "")

    kotlin_info["kotlinc_opts"] = create_struct(**kotlinc_opts_dict)

    kotlin_target_info = create_struct(**kotlin_info)
    info_file = create_proto(target, ctx, kotlin_target_info, "kotlin_target_info")

    return info_file, None
