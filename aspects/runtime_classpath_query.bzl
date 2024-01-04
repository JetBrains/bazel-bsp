def format(target):
    provider = providers(target).get("@@_builtins//:common/java/java_info.bzl%JavaInfo")
    if provider == None:
        provider = providers(target)["JavaInfo"]  #bazel6

    compilation_info = getattr(provider, "compilation_info", None)

    runtime_classpath = []  #bazel5 default to [] because depset() not available
    if compilation_info:
        runtime_classpath = compilation_info.runtime_classpath.to_list()
    elif hasattr(provider, "transitive_runtime_jars"):
        runtime_classpath = provider.transitive_runtime_jars.to_list()

    compile_classpath = []  #bazel5 default to [] because depset() not available
    if (compilation_info and hasattr(compilation_info, "transitive_compile_time_jars")):
        compile_classpath = compilation_info.transitive_compile_time_jars.to_list()
    elif hasattr(provider, "transitive_compile_time_jars"):
        compile_classpath = provider.transitive_compile_time_jars.to_list()

    return {
        #bazel5 returning dict, because struct not available in queries
        "runtime_classpath": [f.path for f in runtime_classpath],
        "compile_classpath": [f.path for f in compile_classpath],
    }
