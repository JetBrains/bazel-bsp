# load("@io_bazel_rules_kotlin//kotlin/internal:defs.bzl", "KtJvmInfo")
# load("@io_bazel_rules_kotlin//kotlin/internal:opts.bzl", "KotlincOptions")
load("//aspects:utils/utils.bzl", "create_proto", "create_struct")

def extract_kotlin_info(target, ctx, **kwargs):
    # if KtJvmInfo not in target:
    #     return None, None

    # provider = target[KtJvmInfo]

    if not hasattr(target, "kt"):
        return None

    provider = target.kt

    # Only supports JVM platform now
    if not hasattr(provider, "language_version"):
        return None, None

    language_version = getattr(provider, "language_version", None)
    api_version = language_version
    associates = getattr(ctx.rule.attr, "associates", [])
    associates_labels = [str(associate.label) for associate in associates]

    # kotlinc_opts_target = getattr(ctx.rule.attr, "kotlinc_opts", None)
    kotlinc_opts = None
    # if kotlinc_opts_target != None and KotlincOptions in kotlinc_opts_target:
    #     kotlinc_opts = kotlinc_opts_target[KotlincOptions]

    kotlin_info = dict(
        language_version = language_version,
        api_version = api_version,
        associates = associates_labels,
    )

    if kotlinc_opts != None:
        kotlin_info["kotlinc_opts"] = kotlinc_opts

    kotlin_target_info = create_struct(**kotlin_info)
    info_file = create_proto(target, ctx, kotlin_target_info, "kotlin_target_info")

    return info_file, None
