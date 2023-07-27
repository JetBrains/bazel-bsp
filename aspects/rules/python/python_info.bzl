load("//aspects:utils/utils.bzl", "create_struct", "file_location")

def extract_python_info(target, ctx):
    if PyInfo not in target:
        return None

    if PyRuntimeInfo in target:
        provider = target[PyRuntimeInfo]
    else:
        provider = None

    return create_struct(
        interpreter = file_location(getattr(provider, "interpreter", None)),
        version = getattr(provider, "python_version", None),
    )
