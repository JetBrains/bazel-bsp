package org.jetbrains.bsp.bazel.server.sync;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.net.URI;
import java.nio.file.Path;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation;
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.server.bsp.utils.SourceRootGuesser;
import org.jetbrains.bsp.bazel.server.sync.dependencytree.DependencyTree;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Language;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

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
      Map<String, TargetInfo> targets, Set<String> rootTargets, ProjectView projectView) {
    languagePluginsService.prepareSync(targets.values());
    var dependencyTree = new DependencyTree(targets, rootTargets);
    var targetsToImport = selectTargetsToImport(rootTargets, targets);
    var modulesFromBazel = createModules(targetsToImport, dependencyTree);
    var workspaceRoot = bazelPathsResolver.workspaceRoot();
    var syntheticModules = createSyntheticModules(modulesFromBazel, workspaceRoot, projectView);
    var allModules = modulesFromBazel.appendAll(syntheticModules);
    var sourceToTarget = buildReverseSourceMapping(modulesFromBazel);
    return new Project(workspaceRoot, allModules, sourceToTarget);
  }

  // When we will be implementing transitive import (configurable through project view),
  // here we will implement the logic to include more targets than the root ones.
  private List<TargetInfo> selectTargetsToImport(
      Set<String> rootTargets, Map<String, TargetInfo> targets) {
    return List.ofAll(rootTargets).flatMap(targets::get);
  }

  private List<Module> createModules(
      List<TargetInfo> targetsToImport, DependencyTree dependencyTree) {
    return targetsToImport
        .map(target -> createModule(target, dependencyTree))
        .filter(module -> !module.tags().contains(Tag.NO_IDE));
  }

  private Module createModule(TargetInfo target, DependencyTree dependencyTree) {
    var label = Label.from(target.getId());
    var directDependencies = resolveDirectDependencies(target);
    var languages = inferLanguages(target);
    var tags = targetKindResolver.resolveTags(target);
    var baseDirectory = bazelPathsResolver.labelToDirectory(label).toUri();
    var sourceSet = resolveSourceSet(target);
    var resources = resolveResources(target);

    var languagePlugin = languagePluginsService.getPlugin(languages);
    var languageData = (Option<LanguageData>) languagePlugin.resolveModule(target);

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

  private List<Label> resolveDirectDependencies(TargetInfo target) {
    return List.ofAll(target.getDependenciesList()).map(dep -> Label.from(dep.getId()));
  }

  private Set<Language> inferLanguages(TargetInfo target) {
    return HashSet.ofAll(target.getSourcesList())
        .flatMap(source -> Language.all().filter(language -> isLanguageFile(source, language)));
  }

  private boolean isLanguageFile(FileLocation file, Language language) {
    return language.getExtensions().exists(ext -> file.getRelativePath().endsWith(ext));
  }

  private SourceSet resolveSourceSet(TargetInfo target) {
    var sources = HashSet.ofAll(target.getSourcesList()).map(bazelPathsResolver::resolve);
    var sourceRoots = sources.map(SourceRootGuesser::getSourcesRoot);
    return new SourceSet(sources.map(Path::toUri), sourceRoots.map(Path::toUri));
  }

  private Set<URI> resolveResources(TargetInfo target) {
    return bazelPathsResolver.resolveUris(target.getResourcesList()).toSet();
  }

  // TODO make this feature configurable with flag in project view file
  private List<Module> createSyntheticModules(
      List<Module> modulesFromBazel, URI workspaceRoot, ProjectView projectView) {
    return new IntelliJProjectTreeViewFix()
        .createModules(workspaceRoot, modulesFromBazel, projectView);
  }

  private Map<URI, Label> buildReverseSourceMapping(List<Module> modules) {
    var output = new java.util.HashMap<URI, Label>();
    modules.forEach(
        module -> {
          module.sourceSet().sources().forEach(source -> output.put(source, module.label()));
          module.resources().forEach(resource -> output.put(resource, module.label()));
        });
    return HashMap.ofAll(output);
  }
}
