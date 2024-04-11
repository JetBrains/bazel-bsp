package org.jetbrains.bsp.bazel.server.sync

import com.google.common.hash.Hashing
import com.google.devtools.build.lib.view.proto.Deps
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.FileLocation
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.dependencygraph.DependencyGraph
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.android.KotlinAndroidModulesMerger
import org.jetbrains.bsp.bazel.server.sync.languages.rust.RustModule
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Language
import org.jetbrains.bsp.bazel.server.sync.model.Library
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.SourceSet
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContext
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.notExists

class BazelProjectMapper(
  private val languagePluginsService: LanguagePluginsService,
  private val bazelPathsResolver: BazelPathsResolver,
  private val targetKindResolver: TargetKindResolver,
  private val kotlinAndroidModulesMerger: KotlinAndroidModulesMerger,
  private val bazelInfo: BazelInfo,
  private val bspClientLogger: BspClientLogger,
  private val metricsLogger: MetricsLogger?
) {

  private fun <T> measure(description: String, body: () -> T): T =
    Measurements.measure(body, description, metricsLogger, bspClientLogger)

  fun createProject(
    targets: Map<String, TargetInfo>,
    rootTargets: Set<String>,
    allTargetNames: List<String>,
    workspaceContext: WorkspaceContext,
    bazelInfo: BazelInfo
  ): Project {
    languagePluginsService.prepareSync(targets.values.asSequence())
    val dependencyGraph = measure("Build dependency tree") {
      DependencyGraph(rootTargets, targets)
    }
    val targetsToImport = measure("Select targets") {
      selectTargetsToImport(workspaceContext, rootTargets, dependencyGraph)
    }
    val targetsAsLibraries = measure("Targets as libraries") {
      targets - targetsToImport.map { it.id }.toSet()
    }
    val annotationProcessorLibraries = measure("Create AP libraries") {
      annotationProcessorLibraries(targetsToImport)
    }
    val kotlinStdlibsMapper = measure("Create kotlin stdlibs") {
      calculateKotlinStdlibsMapper(targetsToImport)
    }
    val scalaLibrariesMapper = measure("Create scala libraries") {
      calculateScalaLibrariesMapper(targetsToImport)
    }
    val androidLibrariesMapper = measure("Create android libraries") {
      calculateAndroidLibrariesMapper(targetsToImport)
    }
    val librariesFromDeps = measure("Merge libraries from deps") {
      concatenateMaps(
        annotationProcessorLibraries,
        kotlinStdlibsMapper,
        scalaLibrariesMapper,
        androidLibrariesMapper,
      )
    }
    val librariesFromDepsAndTargets = measure("Libraries from targets and deps") {
      createLibraries(targetsAsLibraries) + librariesFromDeps.values.flatten().associateBy { it.label }
    }
    val extraLibrariesFromJdeps = measure("Libraries from jdeps") {
      jdepsLibraries(targetsToImport.associateBy { it.id }, librariesFromDeps, librariesFromDepsAndTargets)
    }
    val workspaceRoot = bazelPathsResolver.workspaceRoot()
    val modulesFromBazel = measure("Create modules") {
      createModules(targetsToImport, dependencyGraph, concatenateMaps(librariesFromDeps, extraLibrariesFromJdeps))
    }
    val mergedModulesFromBazel = measure("Merge Kotlin Android modules") {
      kotlinAndroidModulesMerger.mergeKotlinAndroidModules(modulesFromBazel, workspaceContext)
    }
    val sourceToTarget = measure("Build reverse sources") {
      buildReverseSourceMapping(mergedModulesFromBazel)
    }
    val librariesToImport = measure("Merge all libraries") {
      librariesFromDepsAndTargets + extraLibrariesFromJdeps.values.flatten().associateBy { it.label }
    }
    val invalidTargets = measure("Save invalid target labels") {
      (removeDotBazelBspTarget(allTargetNames) - targetsToImport.map(TargetInfo::getId).toSet()).map { Label(it) }
    }
    val rustExternalTargetsToImport = measure("Select external Rust targets") {
      selectRustExternalTargetsToImport(rootTargets, dependencyGraph)
    }
    val rustExternalModules = measure("Create Rust external modules") {
      createRustExternalModules(rustExternalTargetsToImport, dependencyGraph, librariesFromDeps)
    }
    val allModules = mergedModulesFromBazel + rustExternalModules
    return Project(workspaceRoot, allModules.toList(), sourceToTarget, librariesToImport, invalidTargets, bazelInfo.release)
  }

  private fun <K, V> concatenateMaps(vararg maps: Map<K, List<V>>): Map<K, List<V>> =
    maps
      .flatMap { it.keys }
      .distinct()
      .associateWith { key ->
        maps.flatMap { it[key].orEmpty() }
      }

  private fun annotationProcessorLibraries(targetsToImport: Sequence<TargetInfo>): Map<String, List<Library>> {
    return targetsToImport
      .filter { it.jvmTargetInfo.generatedJarsList.isNotEmpty() }
      .associate { targetInfo ->
        targetInfo.id to
          Library(
            label = targetInfo.id + "_generated",
            outputs = targetInfo.jvmTargetInfo.generatedJarsList
              .flatMap { it.binaryJarsList }
              .map { bazelPathsResolver.resolveUri(it) }
              .toSet(),
            sources = targetInfo.jvmTargetInfo.generatedJarsList
              .flatMap { it.sourceJarsList }
              .map { bazelPathsResolver.resolveUri(it) }
              .toSet(),
            dependencies = emptyList(),
            interfaceJars = emptySet(),
          )
      }
      .map { it.key to listOf(it.value) }
      .toMap()
  }

  private fun calculateKotlinStdlibsMapper(targetsToImport: Sequence<TargetInfo>): Map<String, List<Library>> {
    val projectLevelKotlinStdlibsLibrary = calculateProjectLevelKotlinStdlibsLibrary(targetsToImport)
    val kotlinTargetsIds = targetsToImport.filter { it.hasKotlinTargetInfo() }.map { it.id }

    return projectLevelKotlinStdlibsLibrary
      ?.let { stdlibsLibrary -> kotlinTargetsIds.associateWith { listOf(stdlibsLibrary) } }
      .orEmpty()
  }

  private fun calculateProjectLevelKotlinStdlibsLibrary(targetsToImport: Sequence<TargetInfo>): Library? {
    val kotlinStdlibsJars = calculateProjectLevelKotlinStdlibsJars(targetsToImport)

    return if (kotlinStdlibsJars.isNotEmpty()) {
      Library(
        label = "rules_kotlin_kotlin-stdlibs",
        outputs = kotlinStdlibsJars,
        sources = emptySet(),
        dependencies = emptyList(),
      )
    } else null
  }

  private fun calculateProjectLevelKotlinStdlibsJars(targetsToImport: Sequence<TargetInfo>): Set<URI> =
    targetsToImport
      .filter { it.hasKotlinTargetInfo() }
      .map { it.kotlinTargetInfo.stdlibsList }
      .flatMap { it.resolveUris() }
      .toSet()

  private fun calculateScalaLibrariesMapper(targetsToImport: Sequence<TargetInfo>): Map<String, List<Library>> {
    val projectLevelScalaSdkLibraries = calculateProjectLevelScalaLibraries()
    val scalaTargets = targetsToImport.filter { it.hasScalaTargetInfo() }.map { it.id }
    return projectLevelScalaSdkLibraries
      ?.let { libraries -> scalaTargets.associateWith { libraries } }
      .orEmpty()
  }

  private fun calculateProjectLevelScalaLibraries(): List<Library>? {
    val scalaSdkLibrariesJars = getProjectLevelScalaSdkLibrariesJars()
    return if (scalaSdkLibrariesJars.isNotEmpty()) {
      scalaSdkLibrariesJars.map {
        Library(
          label = Paths.get(it).name,
          outputs = setOf(it),
          sources = emptySet(),
          dependencies = emptyList()
        )
      }
    } else null
  }

  private fun getProjectLevelScalaSdkLibrariesJars(): Set<URI> =
    languagePluginsService.scalaLanguagePlugin.scalaSdk
      ?.compilerJars
      ?.toSet().orEmpty()

  private fun calculateAndroidLibrariesMapper(targetsToImport: Sequence<TargetInfo>): Map<String, List<Library>> =
    targetsToImport.mapNotNull { target ->
        val aidlLibrary = createAidlLibrary(target) ?: return@mapNotNull null
        target.id to listOf(aidlLibrary)
      }.toMap()

  private fun createAidlLibrary(target: TargetInfo): Library? {
    if (!target.hasAndroidTargetInfo()) return null
    val androidTargetInfo = target.androidTargetInfo
    if (!androidTargetInfo.hasAidlBinaryJar()) return null

    val libraryLabel = target.id + "_aidl"
    if (target.sourcesList.isEmpty()) {
      // Bazel doesn't create the AIDL jar if there's no sources, since it'd be the same as the output jar
      return createLibrary(libraryLabel, target)
    }

    val outputs = listOf(target.androidTargetInfo.aidlBinaryJar).resolveUris()
    val sources = if (target.androidTargetInfo.hasAidlSourceJar()) {
      listOf(target.androidTargetInfo.aidlSourceJar).resolveUris()
    } else {
      emptySet()
    }
    return Library(
      label = libraryLabel,
      outputs = outputs,
      sources = sources,
      dependencies = emptyList(),
      interfaceJars = emptySet(),
    )
  }

  /**
   * In some cases, the jar dependencies of a target might be injected by bazel or rules and not are not
   * available via `deps` field of a target. For this reason, we read JavaOutputInfo's jdeps file and
   * filter out jars that have not been included in the target's `deps` list.
   *
   * The old Bazel Plugin performs similar step here
   * https://github.com/bazelbuild/intellij/blob/b68ec8b33aa54ead6d84dd94daf4822089b3b013/java/src/com/google/idea/blaze/java/sync/importer/BlazeJavaWorkspaceImporter.java#L256
   */
  private fun jdepsLibraries(targetsToImport: Map<String, TargetInfo>, libraryDependencies: Map<String, List<Library>>, librariesToImport: Map<String, Library>):
    Map<String, List<Library>> {
    val targetsToJdepsJars = getAllJdepsDependencies(targetsToImport, libraryDependencies, librariesToImport)
    val libraryNameToLibraryValueMap = HashMap<String, Library>()
    return targetsToJdepsJars.mapValues {
      it.value.map { lib ->
        val label = syntheticLabel(lib.toString())
        libraryNameToLibraryValueMap.computeIfAbsent(label) { _ ->
          Library(
            label = label,
            dependencies = emptyList(),
            interfaceJars = emptySet(),
            outputs = setOf(bazelPathsResolver.resolveUri(lib)),
            sources = emptySet())
        }
      }
    }
  }

  private fun getAllJdepsDependencies(targetsToImport: Map<String, TargetInfo>,
                                      libraryDependencies: Map<String, List<Library>>,
                                      librariesToImport: Map<String, Library>): Map<String, Set<Path>> =
    targetsToImport
      .filter { targetSupportsJdeps(it.value) }
      .mapValues { targetInfo ->
        val jarsFromDirectDependencies = getAllOutputJarsFromTransitiveDeps(targetInfo,
          targetsToImport,
          libraryDependencies,
          librariesToImport)
        val jarsFromJdeps = dependencyJarsFromJdepsFiles(targetInfo.value)
        jarsFromJdeps - jarsFromDirectDependencies
      }
      .filterValues { it.isNotEmpty() }

  private fun getAllOutputJarsFromTransitiveDeps(
    targetInfo: Map.Entry<String, TargetInfo>,
    targetsToImport: Map<String, TargetInfo>,
    libraryDependencies: Map<String, List<Library>>,
    librariesToImport: Map<String, Library>): Set<Path> {
    return getAllTransitiveDependencies(targetInfo.value, targetsToImport, libraryDependencies, librariesToImport)
      .flatMap { dep ->
        val jarsFromTargets = targetsToImport[dep]?.let { getTargetOutputJars(it) + getTargetInterfaceJars(it) }.orEmpty()
        val jarsFromLibraries = librariesToImport[dep]?.let { it.outputs + it.interfaceJars }.orEmpty().map { Paths.get(it.path) }
        jarsFromTargets + jarsFromLibraries
      }.toSet()
  }

  private fun getAllTransitiveDependencies(target: TargetInfo,
                                           targetsToImport: Map<String, TargetInfo>,
                                           libraryDependencies: Map<String, List<Library>>,
                                           allLibraries: Map<String, Library>): HashSet<String> {
    var toVisit = target.dependenciesList.map { it.id } + libraryDependencies[target.id].orEmpty().map { it.label }
    val visited = HashSet<String>()
    while (toVisit.isNotEmpty()) {
      val current = toVisit.first()
      val dependencyLabels = targetsToImport[current]?.dependenciesList.orEmpty().map { it.id } + allLibraries[current]?.dependencies.orEmpty()
      visited += current
      toVisit = toVisit + dependencyLabels - current - visited
    }
    return visited
  }

  private fun dependencyJarsFromJdepsFiles(targetInfo: TargetInfo): Set<Path> =
    targetInfo.jvmTargetInfo.jdepsList.flatMap {
      val path = bazelPathsResolver.resolve(it)
      if (path.toFile().exists()) {
        val bytes = Files.readAllBytes(path)
        Deps.Dependencies.parseFrom(bytes).dependencyList.map { dependency ->
          bazelPathsResolver.resolveOutput(Paths.get(dependency.path))
        }
      } else {
        emptySet()
      }
    }.toSet()

  private fun targetSupportsJdeps(targetInfo: TargetInfo): Boolean {
    val languages = inferLanguages(targetInfo)
    return setOf(Language.JAVA, Language.KOTLIN, Language.SCALA, Language.ANDROID).containsAll(languages)
  }

  private fun syntheticLabel(lib: String): String {
    val shaOfPath = Hashing.sha256().hashString(lib, StandardCharsets.UTF_8) // just in case of a conflict in filename
    return Paths.get(lib).fileName.toString().replace("[^0-9a-zA-Z]".toRegex(), "-") + "-" + shaOfPath
  }

  private fun createLibraries(targets: Map<String, TargetInfo>): Map<String, Library> {
    return targets.mapValues { (targetId, targetInfo) ->
      createLibrary(targetId, targetInfo)
    }
      .filterValues { it.interfaceJars.isNotEmpty() || it.sources.isNotEmpty() || it.outputs.isNotEmpty() }
  }

  private fun createLibrary(label: String, targetInfo: TargetInfo): Library =
    Library(
      label = label,
      outputs = getTargetJarUris(targetInfo) + getAndroidAarUris(targetInfo),
      sources = getSourceJarUris(targetInfo),
      dependencies = targetInfo.dependenciesList.map { it.id },
      interfaceJars = getTargetInterfaceJars(targetInfo).map { it.toUri() }.toSet(),
    )

  private fun List<FileLocation>.resolveUris() =
    map { bazelPathsResolver.resolve(it).toUri() }.toSet()

  private fun getTargetJarUris(targetInfo: TargetInfo) =
    targetInfo.jvmTargetInfo.jarsList
      .flatMap { it.binaryJarsList }
      .resolveUris()

  private fun getAndroidAarUris(targetInfo: TargetInfo): Set<URI> {
    if (!targetInfo.hasAndroidAarImportInfo()) return emptySet()
    val androidAarImportInfo = targetInfo.androidAarImportInfo

    val result = mutableSetOf<URI>()
    result += bazelPathsResolver.resolve(androidAarImportInfo.manifest).toUri()
    if (androidAarImportInfo.hasResourceFolder()) {
      result += bazelPathsResolver.resolve(androidAarImportInfo.resourceFolder).resolve("res").toUri()
    }
    if (androidAarImportInfo.hasRTxt()) {
      result += bazelPathsResolver.resolve(targetInfo.androidAarImportInfo.rTxt).toUri()
    }
    return result
  }

  private fun getSourceJarUris(targetInfo: TargetInfo) =
    targetInfo.jvmTargetInfo.jarsList
      .flatMap { it.sourceJarsList }
      .resolveUris()

  private fun getTargetOutputJars(targetInfo: TargetInfo) =
    targetInfo.jvmTargetInfo.jarsList
      .flatMap { it.binaryJarsList }
      .map { bazelPathsResolver.resolve(it) }
      .toSet()

  private fun getTargetInterfaceJars(targetInfo: TargetInfo) =
    targetInfo.jvmTargetInfo.jarsList
      .flatMap { it.interfaceJarsList }
      .map { bazelPathsResolver.resolve(it) }
      .toSet()

  private fun selectRustExternalTargetsToImport(
    rootTargets: Set<String>, graph: DependencyGraph
  ): Sequence<TargetInfo> =
    graph.allTargetsAtDepth(-1, rootTargets).asSequence().filter { !isWorkspaceTarget(it) && isRustTarget(it) }

  private fun selectTargetsToImport(
    workspaceContext: WorkspaceContext, rootTargets: Set<String>, graph: DependencyGraph
  ): Sequence<TargetInfo> = graph.allTargetsAtDepth(
    workspaceContext.importDepth.value, rootTargets
  ).asSequence().filter(::isWorkspaceTarget)

  private fun hasKnownSources(targetInfo: TargetInfo) =
    targetInfo.sourcesList.any {
      it.relativePath.endsWith(".java") ||
        it.relativePath.endsWith(".kt") ||
        it.relativePath.endsWith(".scala") ||
        it.relativePath.endsWith(".py") ||
        it.relativePath.endsWith(".sh") ||
        it.relativePath.endsWith(".rs") ||
        it.relativePath.endsWith(".go")
    }

  private fun isWorkspaceTarget(target: TargetInfo): Boolean =
    bazelInfo.release.isRelativeWorkspacePath(target.id) &&
      (hasKnownSources(target) ||
        target.kind in setOf(
        "java_library",
        "java_binary",
        "java_test",
        "kt_jvm_library",
        "kt_jvm_binary",
        "kt_jvm_test",
        "scala_library",
        "scala_binary",
        "scala_test",
        "rust_test",
        "rust_doc",
        "rust_doc_test",
        "android_library",
        "android_binary",
        "android_local_test",
        "go_library",
        "go_binary",
        "go_test",
        )
      )

  private fun isRustTarget(target: TargetInfo): Boolean =
    target.hasRustCrateInfo()

  private fun createModules(
    targetsToImport: Sequence<TargetInfo>,
    dependencyGraph: DependencyGraph,
    generatedLibraries: Map<String, Collection<Library>>,
  ): List<Module> = runBlocking {
    targetsToImport.asFlow()
      .map {
        createModule(
          it,
          dependencyGraph,
          generatedLibraries[it.id].orEmpty())
      }
      .filterNot { it.tags.contains(Tag.NO_IDE) }
      .toList()
  }


  private fun createModule(
    target: TargetInfo,
    dependencyGraph: DependencyGraph,
    extraLibraries: Collection<Library>,
  ): Module {
    val label = Label(target.id)
    val directDependencies = resolveDirectDependencies(target) + extraLibraries.map { Label(it.label) }
    val languages = inferLanguages(target)
    val tags = targetKindResolver.resolveTags(target)
    val baseDirectory = label.toDirectoryUri()
    val languagePlugin = languagePluginsService.getPlugin(languages)
    val sourceSet = resolveSourceSet(target, languagePlugin)
    val resources = resolveResources(target, languagePlugin)
    val languageData = languagePlugin.resolveModule(target)
    val sourceDependencies = languagePlugin.dependencySources(target, dependencyGraph)
    val environment = environmentItem(target)
    return Module(
      label = label,
      isSynthetic = false,
      directDependencies = directDependencies,
      languages = languages,
      tags = tags,
      baseDirectory = baseDirectory,
      sourceSet = sourceSet,
      resources = resources,
      outputs = emptySet(),
      sourceDependencies = sourceDependencies,
      languageData = languageData,
      environmentVariables = environment,
    )
  }

  private fun resolveDirectDependencies(target: TargetInfo): List<Label> =
    target.dependenciesList.map { Label(it.id) }

  private fun inferLanguages(target: TargetInfo): Set<Language> {
    val languagesForTarget = Language.all().filter { isBinaryTargetOfLanguage(target.kind, it) }.toHashSet()
    val languagesForSources = target.sourcesList.flatMap { source: FileLocation ->
      Language.all().filter { isLanguageFile(source, it) }
    }.toHashSet()
    return languagesForTarget + languagesForSources
  }

  private fun isLanguageFile(file: FileLocation, language: Language): Boolean =
    language.extensions.any {
      file.relativePath.endsWith(it)
    }

  private fun isBinaryTargetOfLanguage(kind: String, language: Language): Boolean =
    language.binaryTargets.contains(kind)

  private fun Label.toDirectoryUri(): URI {
    val isWorkspace = bazelPathsResolver.isRelativeWorkspacePath(value)
    val path = if (isWorkspace) bazelPathsResolver.extractRelativePath(value) else bazelPathsResolver.extractExternalPath(value)
    return bazelPathsResolver.pathToDirectoryUri(path, isWorkspace)
  }


  private fun resolveSourceSet(target: TargetInfo, languagePlugin: LanguagePlugin<*>): SourceSet {
    val sources = target.sourcesList.toSet()
      .map(bazelPathsResolver::resolve)
      .onEach { if (it.notExists()) it.logNonExistingFile(target.id) }
      .filter { it.exists() }
    val generatedSources = target.generatedSourcesList.toSet()
      .map(bazelPathsResolver::resolve)
      .onEach { if (it.notExists()) it.logNonExistingFile(target.id) }
      .filter { it.exists() }

    val sourceRoots = (sources + generatedSources).mapNotNull(languagePlugin::calculateSourceRoot)
    return SourceSet(
      sources = sources.map(bazelPathsResolver::resolveUri).toSet(),
      generatedSources =  generatedSources.map(bazelPathsResolver::resolveUri).toSet(),
      sourceRoots = sourceRoots.map(bazelPathsResolver::resolveUri).toSet()
    )
  }

  private fun Path.logNonExistingFile(targetId: String) {
    val message = "[WARN] target $targetId: $this does not exist."
    bspClientLogger.error(message)
  }

  private fun resolveResources(target: TargetInfo, languagePlugin: LanguagePlugin<*>): Set<URI> =
    bazelPathsResolver.resolveUris(target.resourcesList).toSet() + languagePlugin.resolveAdditionalResources(target)

  private fun buildReverseSourceMapping(modules: List<Module>): Map<URI, Label> =
    modules.asSequence().flatMap(::buildReverseSourceMappingForModule).toMap()

  private fun buildReverseSourceMappingForModule(module: Module): List<Pair<URI, Label>> =
    with(module) {
      (sourceSet.sources + resources).map { Pair(it, label) }
    }

  private fun environmentItem(target: TargetInfo): Map<String, String> {
    val inheritedEnvs = collectInheritedEnvs(target)
    val targetEnv = target.envMap
    return inheritedEnvs + targetEnv
  }

  private fun collectInheritedEnvs(targetInfo: TargetInfo): Map<String, String> =
    targetInfo.envInheritList.associateWith { System.getenv(it) }

  private fun removeDotBazelBspTarget(targets: List<String>): List<String> {
    return targets.filter { bazelInfo.release.isRelativeWorkspacePath(it) && !bazelInfo.release.stripPrefix(it).startsWith(".bazelbsp") }
  }

  private fun createRustExternalModules(
    targetsToImport: Sequence<TargetInfo>,
    dependencyGraph: DependencyGraph,
    generatedLibraries: Map<String, Collection<Library>>,
  ): Sequence<Module> {
    val modules = createModules(targetsToImport, dependencyGraph, generatedLibraries)
    return modules.asSequence().onEach {
      if (it.languageData is RustModule) {
        it.languageData.isExternalModule = true
      }
    }
  }
}
