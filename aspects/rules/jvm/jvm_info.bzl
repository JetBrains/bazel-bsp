load("//aspects:utils/utils.bzl", "create_proto", "create_struct", "file_location", "is_external", "map", "update_sync_output_groups")

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
        source_jars = output.source_jars
        if type(source_jars) == "depset":
            return source_jars.to_list()
        else:
            # assuming it returns sequence type here
            return source_jars
    if hasattr(output, "source_jar"):
        return [output.source_jar]
    return []

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

def get_generated_jars(provider):
    if hasattr(provider, "java_outputs"):
        return map_with_resolve_files(to_generated_jvm_outputs, provider.java_outputs)

    if hasattr(provider, "annotation_processing") and provider.annotation_processing and provider.annotation_processing.enabled:
        class_jars = [provider.annotation_processing.class_jar]
        source_jars = [provider.annotation_processing.source_jar]

        # Additional info from `rules_kotlin`'s `KtJvmInfo`
        if hasattr(provider, "additional_generated_source_jars"):
            source_jars = source_jars + [jar for jar in provider.additional_generated_source_jars]
        if hasattr(provider, "all_output_jars"):
            class_jars = class_jars + [jar for jar in provider.all_output_jars]

        output = struct(
            binary_jars = [file_location(jar) for jar in class_jars],
            source_jars = [file_location(jar) for jar in source_jars],
        )
        resolve_files = class_jars + source_jars
        return [output], resolve_files

    return [], []

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
    transitive_compile_time_jars = getattr(provider, "transitive_compile_time_jars", depset())

    return compilation_info.compilation_classpath if compilation_info else transitive_compile_time_jars

def get_jvm_provider(target):
    if hasattr(target, "scala"):
        return target.scala
    if hasattr(target, "kt") and hasattr(target.kt, "outputs"):
        return target.kt
    if JavaInfo in target:
        return target[JavaInfo]
    return None

def extract_jvm_info(target, ctx, output_groups, **kwargs):
    provider = get_jvm_provider(target)
    if not provider:
        return None, None

    if hasattr(provider, "java_outputs") and provider.java_outputs:
        java_outputs = provider.java_outputs
    elif hasattr(provider, "outputs") and provider.outputs:
        java_outputs = provider.outputs.jars
    else:
        return None, None

    # I don't know why, but it seems that the "java_outputs" variable can have a different type, depending on language
    jdeps = get_jdeps(target)

    resolve_files = []

    jars, resolve_files_jars = map_with_resolve_files(to_jvm_outputs, java_outputs)
    resolve_files += resolve_files_jars

    generated_jars, resolve_files_generated_jars = get_generated_jars(provider)
    resolve_files += resolve_files_generated_jars

    javac_opts = getattr(ctx.rule.attr, "javacopts", [])
    jvm_flags = getattr(ctx.rule.attr, "jvm_flags", [])
    args = getattr(ctx.rule.attr, "args", [])
    main_class = getattr(ctx.rule.attr, "main_class", None)

    if (is_external(target)):
        runtime_jars = extract_runtime_jars(target, provider).to_list()
        compile_jars = extract_compile_jars(provider).to_list()
        source_jars = getattr(provider, "transitive_source_jars", depset()).to_list()
        resolve_files += runtime_jars
        resolve_files += compile_jars
        resolve_files += source_jars
        update_sync_output_groups(output_groups, "external-deps-resolve", depset(resolve_files))

    info = create_struct(
        jars = jars,
        generated_jars = generated_jars,
        javac_opts = javac_opts,
        jvm_flags = jvm_flags,
        main_class = main_class,
        args = args,
        jdeps = [file_location(j) for j in jdeps],
    )

    return create_proto(target, ctx, info, "jvm_target_info"), None

def get_jdeps(target):
    if JavaInfo in target:
        return [jo.jdeps for jo in target[JavaInfo].java_outputs if jo.jdeps != None]
    return []
