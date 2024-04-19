def _animals_library_source_impl(ctx):
    tc = ctx.toolchains["@bazel_tools//tools/jdk:toolchain_type"]
    cat_java = ctx.actions.declare_file("Cat.java")
    ctx.actions.write(cat_java, """
                      package org.jetbrains.bsp.example.animals;
                      public class Cat {};
                      """)
    src_jar = ctx.actions.declare_file("animals.srcjar")
    java_common.pack_sources(ctx.actions, sources = [cat_java], java_toolchain = tc.java, output_source_jar = src_jar)

    return [
        DefaultInfo(files = depset([src_jar])),
    ]

animals_library_source = rule(
    implementation = _animals_library_source_impl,
    fragments = ["java"],
    toolchains = ["@bazel_tools//tools/jdk:toolchain_type"],
    attrs = {
        "_java_toolchain": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_java_toolchain"),
        ),
    },
)
