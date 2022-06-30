package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.*;
import io.vavr.control.Option;
import java.net.URI;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Language;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext;

public class BazelProjectMapper {
  private final LanguagePluginsService languagePluginsService;
  private final BazelPathsResolver bazelPathsResolver;
  private final TargetKindResolver targetKindResolver;

  public BazelProjectMapper(
      LanguagePluginsService languagePluginsService,
      BazelPathsResolver bazelPathsResolver,
      TargetKindResolver targetKindResolver) {
    this.languagePluginsService = languagePluginsService;
    this.bazelPathsResolver = bazelPathsResolver;
    this.targetKindResolver = targetKindResolver;
  }

  public Project createProject(
      Map<String, TargetInfo> targets, Set<String> rootTargets, WorkspaceContext workspaceContext) {
    languagePluginsService.prepareSync(targets.values());
    var dependencyTree = new DependencyTree(rootTargets.toJavaSet(), targets.toJavaMap());
    var targetsToImport = selectTargetsToImport(workspaceContext, rootTargets, dependencyTree);
    var modulesFromBazel = createModules(targetsToImport, dependencyTree);
    var workspaceRoot = bazelPathsResolver.workspaceRoot();
    var syntheticModules =
        createSyntheticModules(modulesFromBazel, workspaceRoot, workspaceContext);
    var allModules = modulesFromBazel.appendAll(syntheticModules);
    var sourceToTarget = buildReverseSourceMapping(modulesFromBazel);
    return new Project(workspaceRoot, allModules, sourceToTarget);
  }

  private Seq<TargetInfo> selectTargetsToImport(
      WorkspaceContext workspaceContext, Set<String> rootTargets, DependencyTree tree) {
    return List.ofAll(
            tree.allTargetsAtDepth(
                workspaceContext.getImportDepth().getValue(), rootTargets.toJavaSet()))
        .filter(this::isWorkspaceTarget);
  }

  private boolean isWorkspaceTarget(TargetInfo target) {
    return target.getId().startsWith("//");
  }

  private Seq<Module> createModules(
      Seq<TargetInfo> targetsToImport, DependencyTree dependencyTree) {
    return targetsToImport
        .iterator()
        .map(target -> createModule(target, dependencyTree))
        .filter(module -> !module.tags().contains(Tag.NO_IDE))
        .toArray();
  }

  private Module createModule(TargetInfo target, DependencyTree dependencyTree) {
    var label = Label.from(target.getId());
    var directDependencies = resolveDirectDependencies(target);
    var languages = inferLanguages(target);
    var tags = targetKindResolver.resolveTags(target);
    var baseDirectory = bazelPathsResolver.labelToDirectoryUri(label);

    var languagePlugin = languagePluginsService.getPlugin(languages);
    var languageData = (Option<LanguageData>) languagePlugin.resolveModule(target);

    var sourceSet = resolveSourceSet(target, languagePlugin);
    var resources = resolveResources(target);

    var sourceDependencies = languagePlugin.dependencySources(target, dependencyTree);

    return new Module(
        label,
        false,
        directDependencies,
        languages,
        tags,
        baseDirectory,
        sourceSet,
        resources,
        sourceDependencies,
        languageData);
  }

  private Seq<Label> resolveDirectDependencies(TargetInfo target) {
    return target.getDependenciesList().stream()
        .map(dep -> Label.from(dep.getId()))
        .collect(Array.collector());
  }

  private Set<Language> inferLanguages(TargetInfo target) {
    if (target.getKind().equals("scala_binary")) {
      return HashSet.of(Language.SCALA);
    }

    return target.getSourcesList().stream()
        .flatMap(
            source ->
                Language.all().filter(language -> isLanguageFile(source, language)).toJavaStream())
        .collect(HashSet.collector());
  }

  private boolean isLanguageFile(FileLocation file, Language language) {
    return language.getExtensions().exists(ext -> file.getRelativePath().endsWith(ext));
  }

  private SourceSet resolveSourceSet(TargetInfo target, LanguagePlugin<?> languagePlugin) {
    var sources = HashSet.ofAll(target.getSourcesList()).map(bazelPathsResolver::resolve);
    var sourceRoots = sources.flatMap(languagePlugin::calculateSourceRoot);

    return new SourceSet(
        sources.map(bazelPathsResolver::resolveUri),
        sourceRoots.map(bazelPathsResolver::resolveUri));
  }

  private Set<URI> resolveResources(TargetInfo target) {
    return bazelPathsResolver.resolveUris(target.getResourcesList()).toSet();
  }

  // TODO make this feature configurable with flag in project view file
  private Seq<Module> createSyntheticModules(
      Seq<Module> modulesFromBazel, URI workspaceRoot, WorkspaceContext workspaceContext) {
    return new IntelliJProjectTreeViewFix(bazelPathsResolver)
        .createModules(workspaceRoot, modulesFromBazel, workspaceContext);
  }

  private Map<URI, Label> buildReverseSourceMapping(Seq<Module> modules) {
    var output = new java.util.HashMap<URI, Label>();
    modules.forEach(
        module -> {
          module.sourceSet().sources().forEach(source -> output.put(source, module.label()));
          module.resources().forEach(resource -> output.put(resource, module.label()));
        });
    return HashMap.ofAll(output);
  }
}
