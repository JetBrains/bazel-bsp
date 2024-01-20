load("//aspects:utils/utils.bzl", "create_proto", "create_struct")
load("@io_bazel_rules_go//go:def.bzl", "go_context")

def extract_go_info(target, ctx, **kwargs):
    actions = target.actions
    data_runfiles = target.data_runfiles
    default_runfiles = target.default_runfiles
    files = target.files
    files_to_run = target.files_to_run
    label = target.label
    output_groups = target.output_groups

    dependencies = getattr(ctx.rule.attr, "deps", [])
    importpath = getattr(ctx.rule.attr, "importpath", [])
    pathtype = getattr(ctx.rule.attr, "pathtype", [])
    packages = getattr(ctx.rule.attr, "packages", [])

    go = go_context(ctx)
    if go != None:
        sdk_home_path = go.sdk.go
    else:
        sdk_home_path = None

    go_target_info = create_struct(
        importpath = importpath,
        sdk_home_path = "/bin/go",
    )

    return create_proto(target, ctx, go_target_info, "go_target_info"), None
