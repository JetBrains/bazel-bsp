package org.jetbrains.bsp.bazel.server.sync;

import static org.jetbrains.bsp.bazel.server.sync.BspMappings.getModules;
import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspId;
import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toBspUri;
import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toLabels;
import static org.jetbrains.bsp.bazel.server.sync.BspMappings.toUri;

import ch.epfl.scala.bsp4j.BuildServerCapabilities;
import ch.epfl.scala.bsp4j.BuildTarget;
import ch.epfl.scala.bsp4j.BuildTargetCapabilities;
import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CompileProvider;
import ch.epfl.scala.bsp4j.DependencySourcesItem;
import ch.epfl.scala.bsp4j.DependencySourcesParams;
import ch.epfl.scala.bsp4j.DependencySourcesResult;
import ch.epfl.scala.bsp4j.InitializeBuildResult;
import ch.epfl.scala.bsp4j.InverseSourcesParams;
import ch.epfl.scala.bsp4j.InverseSourcesResult;
import ch.epfl.scala.bsp4j.JavacOptionsItem;
import ch.epfl.scala.bsp4j.JavacOptionsParams;
import ch.epfl.scala.bsp4j.JavacOptionsResult;
import ch.epfl.scala.bsp4j.JvmEnvironmentItem;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmRunEnvironmentResult;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentParams;
import ch.epfl.scala.bsp4j.JvmTestEnvironmentResult;
import ch.epfl.scala.bsp4j.ResourcesItem;
import ch.epfl.scala.bsp4j.ResourcesParams;
import ch.epfl.scala.bsp4j.ResourcesResult;
import ch.epfl.scala.bsp4j.RunProvider;
import ch.epfl.scala.bsp4j.ScalaMainClassesParams;
import ch.epfl.scala.bsp4j.ScalaMainClassesResult;
import ch.epfl.scala.bsp4j.ScalaTestClassesParams;
import ch.epfl.scala.bsp4j.ScalaTestClassesResult;
import ch.epfl.scala.bsp4j.ScalacOptionsParams;
import ch.epfl.scala.bsp4j.ScalacOptionsResult;
import ch.epfl.scala.bsp4j.SourceItem;
import ch.epfl.scala.bsp4j.SourceItemKind;
import ch.epfl.scala.bsp4j.SourcesItem;
import ch.epfl.scala.bsp4j.SourcesParams;
import ch.epfl.scala.bsp4j.SourcesResult;
import ch.epfl.scala.bsp4j.TestProvider;
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult;
import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Option;
import java.util.Collections;
import org.jetbrains.bsp.bazel.commons.Constants;
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService;
import org.jetbrains.bsp.bazel.server.sync.model.Label;
import org.jetbrains.bsp.bazel.server.sync.model.Language;
import org.jetbrains.bsp.bazel.server.sync.model.Module;
import org.jetbrains.bsp.bazel.server.sync.model.Project;
import org.jetbrains.bsp.bazel.server.sync.model.Tag;

public class BspProjectMapper {

  private final LanguagePluginsService languagePluginsService;

  public BspProjectMapper(LanguagePluginsService languagePluginsService) {
    this.languagePluginsService = languagePluginsService;
  }

  public InitializeBuildResult initializeServer(Seq<Language> supportedLanguages) {
    var languageNames = supportedLanguages.map(Language::getName).toJavaList();

    var capabilities = new BuildServerCapabilities();
    capabilities.setCompileProvider(new CompileProvider(languageNames));
    capabilities.setRunProvider(new RunProvider(languageNames));
    capabilities.setTestProvider(new TestProvider(languageNames));
    capabilities.setDependencySourcesProvider(true);
    capabilities.setInverseSourcesProvider(true);
    capabilities.setResourcesProvider(true);
    capabilities.setJvmRunEnvironmentProvider(true);
    capabilities.setJvmTestEnvironmentProvider(true);
    return new InitializeBuildResult(
        Constants.NAME, Constants.VERSION, Constants.BSP_VERSION, capabilities);
  }

  public WorkspaceBuildTargetsResult workspaceTargets(Project project) {
    var buildTargets = project.modules().map(this::toBuildTarget);
    return new WorkspaceBuildTargetsResult(buildTargets.toJavaList());
  }

  private BuildTarget toBuildTarget(Module module) {
    var label = toBspId(module);
    var dependencies = module.directDependencies().map(BspMappings::toBspId);
    var languages = module.languages().flatMap(Language::getAllNames);
    var capabilities = inferCapabilities(module);
    var tags = module.tags().flatMap(BspMappings::toBspTag);
    var baseDirectory = toBspUri(module.baseDirectory());

    var buildTarget =
        new BuildTarget(
            label,
            tags.toJavaList(),
            languages.toJavaList(),
            dependencies.toJavaList(),
            capabilities);
    buildTarget.setDisplayName(label.getUri());
    buildTarget.setBaseDirectory(baseDirectory);
    applyLanguageData(module, buildTarget);

    return buildTarget;
  }

  private BuildTargetCapabilities inferCapabilities(Module module) {
    var canCompile = !module.tags().contains(Tag.NO_BUILD);
    var canTest = module.tags().contains(Tag.TEST);
    var canRun = module.tags().contains(Tag.APPLICATION);
    return new BuildTargetCapabilities(canCompile, canTest, canRun);
  }

  private void applyLanguageData(Module module, BuildTarget buildTarget) {
    var plugin = languagePluginsService.getPlugin(module.languages());
    module.languageData().forEach(data -> plugin.setModuleData(data, buildTarget));
  }

  public SourcesResult sources(Project project, SourcesParams sourcesParams) {
    // TODO handle generated sources. google's plugin doesn't ever mark source root as generated
    // we need a use case with some generated files and then figure out how to handle it
    var labels = toLabels(sourcesParams.getTargets());
    var sourcesItems =
        labels.map(
            label ->
                project
                    .findModule(label)
                    .map(this::toSourcesItem)
                    .getOrElse(() -> emptySourcesItem(label)));
    return new SourcesResult(sourcesItems.toJavaList());
  }

  private SourcesItem toSourcesItem(Module module) {
    var sourceSet = module.sourceSet();
    var sourceItems =
        sourceSet
            .sources()
            .map(source -> new SourceItem(toBspUri(source), SourceItemKind.FILE, false));
    var sourceRoots = sourceSet.sourceRoots().map(BspMappings::toBspUri);

    var sourcesItem = new SourcesItem(toBspId(module), sourceItems.toJavaList());
    sourcesItem.setRoots(sourceRoots.toJavaList());
    return sourcesItem;
  }

  private SourcesItem emptySourcesItem(Label label) {
    return new SourcesItem(toBspId(label), Collections.emptyList());
  }

  public ResourcesResult resources(Project project, ResourcesParams resourcesParams) {
    var labels = toLabels(resourcesParams.getTargets());
    var resourcesItems =
        labels.map(
            label ->
                project
                    .findModule(label)
                    .map(this::toResourcesItem)
                    .getOrElse(() -> emptyResourcesItem(label)));
    return new ResourcesResult(resourcesItems.toJavaList());
  }

  private ResourcesItem toResourcesItem(Module module) {
    var resources = module.resources().map(BspMappings::toBspUri);
    return new ResourcesItem(toBspId(module), resources.toJavaList());
  }

  private ResourcesItem emptyResourcesItem(Label label) {
    return new ResourcesItem(toBspId(label), Collections.emptyList());
  }

  public InverseSourcesResult inverseSources(
      Project project, InverseSourcesParams inverseSourcesParams) {
    var documentUri = toUri(inverseSourcesParams.getTextDocument());
    var targets = project.findTargetBySource(documentUri).map(BspMappings::toBspId).toList();
    return new InverseSourcesResult(targets.toJavaList());
  }

  public DependencySourcesResult dependencySources(
      Project project, DependencySourcesParams dependencySourcesParams) {
    var labels = toLabels(dependencySourcesParams.getTargets());
    var items = labels.map(label -> getDependencySourcesItem(project, label));
    return new DependencySourcesResult(items.toJavaList());
  }

  private DependencySourcesItem getDependencySourcesItem(Project project, Label label) {
    var sources =
        project
            .findModule(label)
            .map(module -> module.sourceDependencies().map(BspMappings::toBspUri))
            .getOrElse(HashSet.empty());
    return new DependencySourcesItem(toBspId(label), sources.toJavaList());
  }

  public JvmRunEnvironmentResult jvmRunEnvironment(
      Project project, JvmRunEnvironmentParams params) {
    var targets = params.getTargets();
    var result = getJvmEnvironmentItems(project, targets).toJavaList();
    return new JvmRunEnvironmentResult(result);
  }

  public JvmTestEnvironmentResult jvmTestEnvironment(
      Project project, JvmTestEnvironmentParams params) {
    var targets = params.getTargets();
    var result = getJvmEnvironmentItems(project, targets).toJavaList();
    return new JvmTestEnvironmentResult(result);
  }

  private Set<JvmEnvironmentItem> getJvmEnvironmentItems(
      Project project, java.util.List<BuildTargetIdentifier> targets) {
    var labels = toLabels(targets);
    return labels.flatMap(
        label -> project.findModule(label).flatMap(this::extractJvmEnvironmentItem));
  }

  private Option<JvmEnvironmentItem> extractJvmEnvironmentItem(Module module) {
    var javaLanguagePlugin = languagePluginsService.javaPlugin();
    return languagePluginsService
        .extractJavaModule(module)
        .map(javaModule -> javaLanguagePlugin.toJvmEnvironmentItem(module, javaModule));
  }

  public JavacOptionsResult buildTargetJavacOptions(Project project, JavacOptionsParams params) {
    var modules = getModules(project, params.getTargets());
    var items = modules.flatMap(this::extractJavacOptionsItem);
    return new JavacOptionsResult(items.toJavaList());
  }

  private Option<JavacOptionsItem> extractJavacOptionsItem(Module module) {
    var javaLanguagePlugin = languagePluginsService.javaPlugin();
    return languagePluginsService
        .extractJavaModule(module)
        .map(javaModule -> javaLanguagePlugin.toJavacOptionsItem(module, javaModule));
  }

  public ScalacOptionsResult buildTargetScalacOptions(Project project, ScalacOptionsParams params) {
    var modules = getModules(project, params.getTargets());
    var scalaLanguagePlugin = languagePluginsService.scalaPlugin();
    var items = modules.flatMap(scalaLanguagePlugin::toScalacOptionsItem);
    return new ScalacOptionsResult(items.toJavaList());
  }

  public ScalaTestClassesResult buildTargetScalaTestClasses(
      Project project, ScalaTestClassesParams params) {
    var modules = getModules(project, params.getTargets());
    var scalaLanguagePlugin = languagePluginsService.scalaPlugin();
    var items = modules.flatMap(scalaLanguagePlugin::toScalaTestClassesItem);
    return new ScalaTestClassesResult(items.toJavaList());
  }

  public ScalaMainClassesResult buildTargetScalaMainClasses(
      Project project, ScalaMainClassesParams params) {
    var modules = getModules(project, params.getTargets());
    var scalaLanguagePlugin = languagePluginsService.scalaPlugin();
    var items = modules.flatMap(scalaLanguagePlugin::toScalaMainClassesItem);
    return new ScalaMainClassesResult(items.toJavaList());
  }
}
