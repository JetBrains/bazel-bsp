load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "to_file_location")

def extract_java_toolchain(target, ctx, dep_targets, **kwargs):
    toolchain = None

    if hasattr(target, "java_toolchain"):
        toolchain = target.java_toolchain
    elif java_common.JavaToolchainInfo != platform_common.ToolchainInfo and \
         java_common.JavaToolchainInfo in target:
        toolchain = target[java_common.JavaToolchainInfo]

    toolchain_info = None
    if toolchain != None:
        java_home = to_file_location(toolchain.java_runtime.java_home, "", False, True) if hasattr(toolchain, "java_runtime") else None
        toolchain_info = create_struct(
            source_version = toolchain.source_version,
            target_version = toolchain.target_version,
            java_home = java_home,
        )
    else:
        for dep in dep_targets:
            if hasattr(dep.bsp_info, "java_toolchain_info"):
                toolchain_info = dep.bsp_info.java_toolchain_info
                break

    if toolchain_info != None:
        info_file = create_proto(target, ctx, toolchain_info, "java_toolchain_info")
        return info_file, dict(java_toolchain_info = toolchain_info)
    else:
        return None, None

JAVA_RUNTIME_TOOLCHAIN_TYPE = "@bazel_tools//tools/jdk:runtime_toolchain_type"

def extract_java_runtime(target, ctx, dep_targets, **kwargs):
    runtime = None

    if java_common.JavaRuntimeInfo in target:  # Bazel 5.4.0 way
        runtime = target[java_common.JavaRuntimeInfo]
    elif JAVA_RUNTIME_TOOLCHAIN_TYPE in ctx.toolchains:  # Bazel 6.0.0 way
        runtime = ctx.toolchains[JAVA_RUNTIME_TOOLCHAIN_TYPE].java_runtime
    else:
        runtime_jdk = getattr(ctx.rule.attr, "runtime_jdk", None)
        if runtime_jdk and java_common.JavaRuntimeInfo in runtime_jdk:
            runtime = runtime_jdk[java_common.JavaRuntimeInfo]

    runtime_info = None
    if runtime != None:
        java_home = to_file_location(runtime.java_home, "", False, True) if hasattr(runtime, "java_home") else None
        runtime_info = create_struct(java_home = java_home)
    else:
        for dep in dep_targets:
            if hasattr(dep.bsp_info, "java_runtime_info"):
                runtime_info = dep.bsp_info.java_runtime_info
                break

    if runtime_info != None:
        info_file = create_proto(target, ctx, runtime_info, "java_runtime_info")
        return info_file, dict(java_runtime_info = runtime_info)
    else:
        return None, None
