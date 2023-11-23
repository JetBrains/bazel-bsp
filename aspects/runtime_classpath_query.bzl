def format(target):
    provider = providers(target)["JavaInfo"]
    compilation_info = getattr(provider, "compilation_info", None)

    result = []
    if compilation_info:
        result = compilation_info.runtime_classpath.to_list()
    elif hasattr(provider, "transitive_runtime_jars"):
        result = provider.transitive_runtime_jars.to_list()
    return "\n".join([f.path for f in result])
