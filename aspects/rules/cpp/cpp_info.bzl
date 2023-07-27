load("//aspects:utils/utils.bzl", "create_struct")

def extract_cpp_info(target, ctx):
    if CcInfo not in target:
        return None

    return create_struct(
        copts = getattr(ctx.rule.attr, "copts", []),
        defines = getattr(ctx.rule.attr, "defines", []),
        link_opts = getattr(ctx.rule.attr, "linkopts", []),
        link_shared = getattr(ctx.rule.attr, "linkshared", False),
    )
