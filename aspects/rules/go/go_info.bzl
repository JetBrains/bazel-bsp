load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "file_location")
load("@io_bazel_rules_go//go:def.bzl", "go_context", "GoArchive")

def extract_go_info(target, ctx, **kwargs):
    if GoArchive not in target:
        return None, None
    go_archive = target[GoArchive]
    importpath = go_archive.data.importpath
    sdk_home_path = _extract_sdk(ctx)

    go_target_info = create_struct(
        importpath = importpath,
        sdk_home_path = sdk_home_path,
    )

    return create_proto(target, ctx, go_target_info, "go_target_info"), None

def _extract_sdk(ctx):
    go = go_context(ctx)
    if go == None:
        return None
    return file_location(go.sdk.go)
