package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaModule;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;

public class ModuleMerger {
  private final Map<Label, Label> remapping = new HashMap<>();

  public Seq<Module> mergeModules(Seq<Module> modules) {
    var modulesByBasePath =
        modules.groupBy(Module::baseDirectory).mapValues(this::mergeModule).values();

    return remapMergedDependencies(modulesByBasePath);
  }

  private Seq<Module> remapMergedDependencies(Seq<Module> modules) {
    return modules.map(this::remapMergedDependencies).toList();
  }

  private Module remapMergedDependencies(Module module) {
    var remappedDeps =
        module
            .directDependencies()
            .iterator()
            .map(dep -> remapping.getOrDefault(dep, dep))
            .filter(dep -> !dep.equals(module.label()))
            .toArray();

    return new Module(
        module.label(),
        module.isSynthetic(),
        remappedDeps,
        module.languages(),
        module.tags(),
        module.baseDirectory(),
        module.sourceSet(),
        module.resources(),
        module.sourceDependencies(),
        module.languageData());
  }

  private Module mergeModule(Seq<Module> modules) {
    if (modules.size() == 1) {
      return modules.head();
    }

    return mergeMany(modules);
  }

  private Label makeMergedLabel(Label label) {
    var colon = label.getValue().lastIndexOf(':');
    if (colon == -1) {
      return label;
    }
    return new Label(label.getValue().substring(0, colon) + ":ezpass-merged");
  }

  private Module mergeMany(Seq<Module> modules) {
    var label = makeMergedLabel(modules.head().label());
    var mergedSources =
        new SourceSet(
            modules.iterator().flatMap(m -> m.sourceSet().sources()).toSet(),
            modules.iterator().flatMap(m -> m.sourceSet().sourceRoots()).toSet());

    if (mergedSources
        .sourceRoots()
        .exists(uri -> uri.toString().endsWith("tools/src/main/scala/"))) {
      mergedSources = new SourceSet(HashSet.empty(), HashSet.of(modules.head().baseDirectory()));
    }

    modules.forEach(mod -> remapping.put(mod.label(), label));

    return new Module(
        label,
        modules.forAll(Module::isSynthetic),
        modules.flatMap(Module::directDependencies).distinct(),
        modules.iterator().flatMap(Module::languages).toSet(),
        modules.iterator().flatMap(Module::tags).toSet(),
        modules.head().baseDirectory(),
        mergedSources,
        modules.iterator().flatMap(Module::resources).toSet(),
        modules.iterator().flatMap(Module::sourceDependencies).toSet(),
        mergeLanguageData(modules.flatMap(Module::languageData)));
  }

  private Option<LanguageData> mergeLanguageData(Seq<LanguageData> languageData) {
    if (languageData.isEmpty()) {
      return Option.none();
    }

    var acc = languageData.head();
    for (var ld : languageData.tail()) {
      acc = mergeTwoLanguages(acc, ld);
    }
    return Option.of(acc);
  }

  private LanguageData mergeTwoLanguages(LanguageData a, LanguageData b) {
    if (a instanceof ScalaModule && b instanceof ScalaModule) {
      return mergeScalaScala((ScalaModule) a, (ScalaModule) b);
    } else if (a instanceof ScalaModule && b instanceof JavaModule) {
      return mergeScalaJava((ScalaModule) a, (JavaModule) b);
    } else if (a instanceof JavaModule && b instanceof ScalaModule) {
      return mergeScalaJava((ScalaModule) b, (JavaModule) a);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private LanguageData mergeScalaScala(ScalaModule a, ScalaModule b) {
    return a;
  }

  private LanguageData mergeScalaJava(ScalaModule a, JavaModule b) {
    return a;
  }
}
