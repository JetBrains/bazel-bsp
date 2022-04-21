package org.jetbrains.bsp.bazel.server.sync.languages;

import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule;
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.model.Language;
import org.jetbrains.bsp.bazel.server.sync.model.Module;

public class LanguagePluginsService {
  private final ScalaLanguagePlugin scalaLanguagePlugin;
  private final JavaLanguagePlugin javaLanguagePlugin;
  private final CppLanguagePlugin cppLanguagePlugin;
  private final ThriftLanguagePlugin thriftLanguagePlugin;
  private final EmptyLanguagePlugin emptyLanguagePlugin;

  public LanguagePluginsService(
      ScalaLanguagePlugin scalaLanguagePlugin,
      JavaLanguagePlugin javaLanguagePlugin,
      CppLanguagePlugin cppLanguagePlugin,
      ThriftLanguagePlugin thriftLanguagePlugin) {
    this.scalaLanguagePlugin = scalaLanguagePlugin;
    this.javaLanguagePlugin = javaLanguagePlugin;
    this.cppLanguagePlugin = cppLanguagePlugin;
    this.thriftLanguagePlugin = thriftLanguagePlugin;
    this.emptyLanguagePlugin = new EmptyLanguagePlugin();
  }

  public void prepareSync(Seq<TargetInfo> targetInfos) {
    scalaLanguagePlugin.prepareSync(targetInfos);
    javaLanguagePlugin.prepareSync(targetInfos);
    cppLanguagePlugin.prepareSync(targetInfos);
    thriftLanguagePlugin.prepareSync(targetInfos);
  }

  public LanguagePlugin<?> getPlugin(Set<Language> languages) {
    if (languages.contains(Language.SCALA)) {
      return scalaLanguagePlugin;
    } else if (languages.contains(Language.JAVA) || languages.contains(Language.KOTLIN)) {
      return javaLanguagePlugin;
      // TODO https://youtrack.jetbrains.com/issue/BAZEL-25
      //    } else if (languages.contains(Language.CPP)) {
      //      return cppLanguagePlugin;
    } else if (languages.contains(Language.THRIFT)) {
      return thriftLanguagePlugin;
    } else {
      return emptyLanguagePlugin;
    }
  }

  public JavaLanguagePlugin javaPlugin() {
    return javaLanguagePlugin;
  }

  public ScalaLanguagePlugin scalaPlugin() {
    return scalaLanguagePlugin;
  }

  public Option<JavaModule> extractJavaModule(Module module) {
    return module
        .languageData()
        .flatMap(
            obj -> {
              if (obj instanceof JavaModule) {
                return Option.some((JavaModule) obj);
              } else if (obj instanceof ScalaModule) {
                return ((ScalaModule) obj).javaModule();
              } else {
                return Option.none();
              }
            });
  }
}
