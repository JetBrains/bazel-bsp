load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "file_location")
load("@io_bazel_rules_go//go:def.bzl", "go_context")

def extract_sdk(ctx):
    go = go_context(ctx)
    if go == None:
        return None
    return file_location(go.sdk.root_file)

def extract_go_info(target, ctx, **kwargs):
    importpath = getattr(ctx.rule.attr, "importpath", [])
    sdk_home_path = extract_sdk(ctx)

    go_target_info = create_struct(
        importpath = importpath,
        sdk_home_path = sdk_home_path,
    )

    return create_proto(target, ctx, go_target_info, "go_target_info"), None
