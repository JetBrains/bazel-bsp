package org.jetbrains.bsp.bazel.server.sync.languages.cpp;

import io.vavr.collection.Seq;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;

public class CppCompilerResolver {
    private final BazelPathsResolver bazelPathsResolver;

    public CppCompilerResolver(BazelPathsResolver bazelPathsResolver) {
        this.bazelPathsResolver = bazelPathsResolver;
    }

    public Option<CppCompiler> resolve(Seq<TargetInfo> targetInfos) {
        // TODO: a better way to find the right compiler?
        return targetInfos.flatMap(this::resolve).headOption();
    }

    private Option<CppCompiler> resolve(TargetInfo targetInfo) {
        if (!targetInfo.hasCcToolchainInfo()) {
            return Option.none();
        }

        var cppToolchainInfo = targetInfo.getCcToolchainInfo();
        var version = cppToolchainInfo.getVersion();
        var compilerType = cppToolchainInfo.getCompilerType();
        var cCompiler = cppToolchainInfo.getCCompiler();
        var cppCompiler = cppToolchainInfo.getCppCompiler();

        var cCompilerUri = bazelPathsResolver.resolveUri(cCompiler);
        var cppCompilerUri = bazelPathsResolver.resolveUri(cppCompiler);

        return Option.some(new CppCompiler(version, compilerType, cCompilerUri, cppCompilerUri));
    }
}
