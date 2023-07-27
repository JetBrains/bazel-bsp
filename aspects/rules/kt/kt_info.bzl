load("//aspects:utils/utils.bzl", "create_struct")

def extract_kotlin_info(target, ctx):
    # if KtJvmInfo not in target:
    #    return None

    # provider = target[KtJvmInfo]

    if not hasattr(target, "kt"):
        return None

    provider = target.kt

    # Only supports JVM platform now
    if not hasattr(provider, "language_version"):
        return None

    language_version = getattr(provider, "language_version", None)
    api_version = language_version
    associates = getattr(ctx.rule.attr, "associates", [])
    associates_labels = [str(associate.label) for associate in associates]

    # kotlinc_opts_target = getattr(ctx.rule.attr, "kotlinc_opts", None)
    kotlinc_opts = None
    # if kotlinc_opts_target != None and KotlincOptions in kotlinc_opts_target:
    #    kotlinc_opts = kotlinc_opts_target[KotlincOptions]

    kotlin_info = dict(
        language_version = language_version,
        api_version = api_version,
        associates = associates_labels,
    )

    if kotlinc_opts != None:
        kotlin_info["kotlinc_opts"] = kotlinc_opts

    return create_struct(**kotlin_info)
