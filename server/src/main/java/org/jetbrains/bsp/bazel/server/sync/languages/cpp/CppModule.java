package org.jetbrains.bsp.bazel.server.sync.languages.cpp;

import io.vavr.collection.List;

import java.util.Objects;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;

public class CppModule implements LanguageData {
    private final CppCompiler compiler;
    private final List<String> copts;
    private final List<String> defines;
    private final List<String> linkopts;
    private final boolean linkshared;

    public CppModule(CppCompiler compiler, List<String> copts, List<String> defines, List<String> linkopts, boolean linkshared) {
        this.compiler = compiler;
        this.copts = copts;
        this.defines = defines;
        this.linkopts = linkopts;
        this.linkshared = linkshared;
    }

    public CppCompiler compiler() {
        return compiler;
    }

    public List<String> copts() {
        return copts;
    }

    public List<String> defines() {
        return defines;
    }

    public List<String> linkopts() {
        return linkopts;
    }

    public boolean linkshared() {
        return linkshared;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CppModule cppModule = (CppModule) o;
        return linkshared == cppModule.linkshared && Objects.equals(compiler, cppModule.compiler) && Objects.equals(copts, cppModule.copts) && Objects.equals(defines, cppModule.defines) && Objects.equals(linkopts, cppModule.linkopts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compiler, copts, defines, linkopts, linkshared);
    }
}
