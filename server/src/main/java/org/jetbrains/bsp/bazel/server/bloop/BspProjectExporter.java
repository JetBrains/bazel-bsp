package org.jetbrains.bsp.bazel.server.bloop;

import com.google.common.collect.Maps;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaModule;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;

class BspProjectExporter {
  private static final Set<Path> IGNORED_SOURCES =
      HashSet.of(
          Paths.get(
              "tools/src/main/scala/com/twitter/bazel/resources_workaround/placeholder.scala"));

  private final Project project;
  private final Path bloopRoot;

  public BspProjectExporter(Project project, Path bloopRoot) {
    this.project = project;
    this.bloopRoot = bloopRoot;
  }

  public Set<Path> export() {
    BloopProjectWriter bloopProjectWriter = new BloopProjectWriter(bloopRoot);
    ClasspathRewriter classpathRewriter = buildClassPathRewriter();
    SourceSetRewriter sourceSetRewriter = new SourceSetRewriter(IGNORED_SOURCES);

    return project
        .modules()
        .iterator()
        .map(
            mod ->
                new BspModuleExporter(
                        project,
                        mod,
                        bloopRoot,
                        bloopProjectWriter,
                        classpathRewriter,
                        sourceSetRewriter)
                    .export())
        .toSet();
  }

  private ClasspathRewriter buildClassPathRewriter() {
    java.util.Map<URI, URI> localArtifactsBuilder = Maps.newHashMap();
    for (var mod : project.modules()) {
      var moduleOutput = classesOutputForModule(mod, bloopRoot);
      for (var ld : mod.languageData()) {
        for (var art : artifactFromLanguageData(ld)) {
          localArtifactsBuilder.put(art, moduleOutput);
        }
      }
    }

    return new ClasspathRewriter(HashMap.ofAll(localArtifactsBuilder));
  }

  private Set<URI> artifactFromLanguageData(LanguageData languageData) {
    return JavaModule.fromLanguageData(languageData).map(JavaModule::mainOutput).toSet();
  }

  private URI classesOutputForModule(Module mod, Path bloopRoot) {
    return bloopRoot.resolve(Naming.compilerOutputNameFor(mod.label())).resolve("classes").toUri();
  }
}
