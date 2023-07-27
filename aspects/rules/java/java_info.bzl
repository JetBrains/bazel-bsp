load("//aspects:utils/java_utils.bzl", "get_java_provider")
load("//aspects:utils/utils.bzl", "create_struct", "file_location", "map", "to_file_location", "update_sync_output_groups")

def map_with_resolve_files(f, xs):
    results = []
    resolve_files = []

    for x in xs:
        if x != None:
            res = f(x)
            if res != None:
                a, b = res
                if a != None:
                    results.append(a)
                if b != None:
                    resolve_files += b

    return results, resolve_files

def get_interface_jars(output):
    if hasattr(output, "compile_jar") and output.compile_jar:
        return [output.compile_jar]
    elif hasattr(output, "ijar") and output.ijar:
        return [output.ijar]
    else:
        return []

def get_source_jars(output):
    if hasattr(output, "source_jars"):
        return output.source_jars
    if hasattr(output, "source_jar"):
        return [output.source_jar]
    return []

def get_generated_jars(provider):
    if hasattr(provider, "java_outputs"):
        return map_with_resolve_files(to_generated_jvm_outputs, provider.java_outputs)

    if hasattr(provider, "annotation_processing") and provider.annotation_processing and provider.annotation_processing.enabled:
        class_jar = provider.annotation_processing.class_jar
        source_jar = provider.annotation_processing.source_jar
        output = struct(
            binary_jars = [file_location(class_jar)],
            source_jars = [file_location(source_jar)],
        )
        resolve_files = [class_jar, source_jar]
        return [output], resolve_files

    return [], []

def to_generated_jvm_outputs(output):
    if output == None or output.generated_class_jar == None:
        return None

    class_jar = output.generated_class_jar
    source_jar = output.generated_source_jar

    output = struct(
        binary_jars = [file_location(class_jar)],
        source_jars = [file_location(source_jar)],
    )
    resolve_files = [class_jar, source_jar]
    return output, resolve_files

def to_jvm_outputs(output):
    if output == None or output.class_jar == None:
        return None

    binary_jars = [output.class_jar]
    interface_jars = get_interface_jars(output)
    source_jars = get_source_jars(output)
    output = struct(
        binary_jars = map(file_location, binary_jars),
        interface_jars = map(file_location, interface_jars),
        source_jars = map(file_location, source_jars),
    )
    resolve_files = binary_jars + interface_jars + source_jars
    return output, resolve_files

def extract_runtime_jars(target, provider):
    compilation_info = getattr(provider, "compilation_info", None)

    if compilation_info:
        return compilation_info.runtime_classpath

    return getattr(provider, "transitive_runtime_jars", target[JavaInfo].transitive_runtime_jars)

def extract_compile_jars(provider):
    compilation_info = getattr(provider, "compilation_info", None)
    transitive_compile_time_jars = getattr(provider, "transitive_compile_time_jars", [])

    return compilation_info.compilation_classpath if compilation_info else transitive_compile_time_jars

def extract_java_info(target, ctx, output_groups):
    provider = get_java_provider(target)
    if not provider:
        return None

    if hasattr(provider, "java_outputs") and provider.java_outputs:
        java_outputs = provider.java_outputs
    elif hasattr(provider, "outputs") and provider.outputs:
        java_outputs = provider.outputs.jars
    else:
        return None

    resolve_files = []

    jars, resolve_files_jars = map_with_resolve_files(to_jvm_outputs, java_outputs)
    resolve_files += resolve_files_jars

    generated_jars, resolve_files_generated_jars = get_generated_jars(provider)
    resolve_files += resolve_files_generated_jars

    runtime_jars = extract_runtime_jars(target, provider).to_list()
    compile_jars = extract_compile_jars(provider).to_list()
    source_jars = provider.transitive_source_jars.to_list()
    resolve_files += runtime_jars
    resolve_files += compile_jars
    resolve_files += source_jars

    runtime_classpath = map(file_location, runtime_jars)
    compile_classpath = map(file_location, compile_jars)
    source_classpath = map(file_location, source_jars)

    javac_opts = getattr(ctx.rule.attr, "javacopts", [])
    jvm_flags = getattr(ctx.rule.attr, "jvm_flags", [])
    args = getattr(ctx.rule.attr, "args", [])
    main_class = getattr(ctx.rule.attr, "main_class", None)

    update_sync_output_groups(output_groups, "bsp-ide-resolve", depset(resolve_files))

    return create_struct(
        jars = jars,
        generated_jars = generated_jars,
        runtime_classpath = runtime_classpath,
        compile_classpath = compile_classpath,
        source_classpath = source_classpath,
        javac_opts = javac_opts,
        jvm_flags = jvm_flags,
        main_class = main_class,
        args = args,
    )

def extract_java_toolchain(target, ctx, dep_targets):
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
        return toolchain_info, dict(java_toolchain_info = toolchain_info)
    else:
        return None, dict()

JAVA_RUNTIME_TOOLCHAIN_TYPE = "@bazel_tools//tools/jdk:runtime_toolchain_type"

def extract_java_runtime(target, ctx, dep_targets):
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
        return runtime_info, dict(java_runtime_info = runtime_info)
    else:
        return None, dict()
