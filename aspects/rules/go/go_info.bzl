load("//aspects:utils/utils.bzl", "create_proto", "create_struct")
load("@io_bazel_rules_go//go:def.bzl", "GoSDK", "GoLibrary", "go_library")

def extract_go_info(target, ctx, **kwargs):
    print(target)

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

    if GoSDK in target:
        sdk_home_path = target[GoSDK]
    else:
        sdk_home_path = None

    go_target_info = create_struct(
        importpath = importpath,
        sdk_home_path = sdk_home_path,
    )

    return create_proto(target, ctx, go_target_info, "go_target_info"), None

