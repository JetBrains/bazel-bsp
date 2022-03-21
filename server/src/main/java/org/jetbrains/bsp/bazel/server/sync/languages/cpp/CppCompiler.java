package org.jetbrains.bsp.bazel.server.sync.languages.cpp;

import java.net.URI;
import java.util.Objects;

public class CppCompiler {
    private final String version;
    private final String compilerType;
    private final URI cCompiler;
    private final URI cppCompiler;

    public CppCompiler(String version, String compilerType, URI cCompiler, URI cppCompiler) {
        this.version = version;
        this.compilerType = compilerType;
        this.cCompiler = cCompiler;
        this.cppCompiler = cppCompiler;
    }

    public String version() {
        return version;
    }

    public String compilerType() {
        return compilerType;
    }

    public URI cCompiler() {
        return cCompiler;
    }

    public URI cppCompiler() {
        return cppCompiler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CppCompiler that = (CppCompiler) o;
        return Objects.equals(version, that.version) && Objects.equals(compilerType, that.compilerType) && Objects.equals(cCompiler, that.cCompiler) && Objects.equals(cppCompiler, that.cppCompiler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, compilerType, cCompiler, cppCompiler);
    }
}
