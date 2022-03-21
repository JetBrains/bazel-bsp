package org.jetbrains.bsp.bazel.server.sync.languages.cpp;

import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetDataKind;
import ch.epfl.scala.bsp4j.CppBuildTarget;
import ch.epfl.scala.bsp4j.CppOptionsItem;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.model.Module;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId;

public class CppLanguagePlugin extends LanguagePlugin<CppModule> {
  private final BazelPathsResolver bazelPathsResolver;
  private final CppCompilerResolver cppCompilerResolver;
  private Option<CppCompiler> cppCompiler = Option.none();

  public CppLanguagePlugin(BazelPathsResolver bazelPathsResolver) {
    this.bazelPathsResolver = bazelPathsResolver;
    this.cppCompilerResolver = new CppCompilerResolver(bazelPathsResolver);
  }

  @Override
  public void prepareSync(Seq<TargetInfo> targetInfos) {
    cppCompiler = cppCompilerResolver.resolve(targetInfos);
  }

  private CppCompiler getCppCompiler() {
    return cppCompiler.getOrElseThrow(
            () -> new RuntimeException("Failed to resolve the C/C++ compiler for the project"));
  }

  @Override
  public Option<CppModule> resolveModule(TargetInfo targetInfo) {
    if (!targetInfo.hasCcTargetInfo()) {
      return Option.none();
    }

    var cppTargetInfo = targetInfo.getCcTargetInfo();
    var cppCompiler = getCppCompiler();
    var copts = List.ofAll(cppTargetInfo.getCoptsList());
    var defines = List.ofAll(cppTargetInfo.getDefinesList());
    var linkopts = List.ofAll(cppTargetInfo.getLinkoptsList());
    var linkshared = cppTargetInfo.getLinkshared();

    var module = new CppModule(cppCompiler, copts, defines, linkopts, linkshared);
    return Option.some(module);
  }

  @Override
  public void applyModuleData(CppModule moduleData, BuildTarget buildTarget) {
    var compiler = moduleData.compiler();

    var cppBuildTarget =
        new CppBuildTarget(
            compiler.version(),
            compiler.compilerType(),
            compiler.cCompiler().toString(),
            compiler.cppCompiler().toString());

    buildTarget.setDataKind(BuildTargetDataKind.CPP);
    buildTarget.setData(cppBuildTarget);
  }

  public Option<CppOptionsItem> toCppOptionsItem(Module module) {
    return module.languageData()
            .flatMap(m -> m instanceof CppModule ? Option.of((CppModule) m) : Option.none())
            .map(m -> {
              var cppOptionsItem = new CppOptionsItem(toBspId(module), m.copts().asJava(), m.defines().asJava(), m.linkopts().asJava());
              cppOptionsItem.setLinkshared(m.linkshared());
              return cppOptionsItem;
            });
    }
}
