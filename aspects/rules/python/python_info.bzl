load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "file_location")

def extract_python_info(target, ctx, **kwargs):
    if PyInfo not in target:
        return None, None

    if PyRuntimeInfo in target:
        provider = target[PyRuntimeInfo]
    else:
        provider = None, None

    python_target_info = create_struct(
        interpreter = file_location(getattr(provider, "interpreter", None)),
        version = getattr(provider, "python_version", None),
    )

    return create_proto(target, ctx, python_target_info, "python_target_info"), None
