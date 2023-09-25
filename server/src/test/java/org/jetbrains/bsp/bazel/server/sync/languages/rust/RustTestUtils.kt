package org.jetbrains.bsp.bazel.server.sync.languages.rust

import org.jetbrains.bsp.bazel.server.sync.model.*
import java.net.URI

fun createModule(
    label: String,
    directDependencies: List<Label>,
    baseDirectory: URI,
    sources: Set<URI>,
    rustModule: RustModule
): Module =
    Module(
        label = Label(label),
        isSynthetic = false,
        directDependencies = directDependencies,
        languages = setOf(Language.RUST),
        tags = setOf(Tag.APPLICATION),
        baseDirectory = baseDirectory,
        sourceSet = SourceSet(
            sources = sources,
            sourceRoots = setOf<URI>()
        ),
        resources = setOf<URI>(),
        outputs = setOf<URI>(),
        sourceDependencies = setOf<URI>(),
        languageData = rustModule,
        environmentVariables = mapOf<String, String>()
    )

fun createRustModule(
    crateId: String,
    crateRoot: String,
    location: RustCrateLocation = RustCrateLocation.EXEC_ROOT,
    crateFeatures: List<String> = emptyList(),
    dependencies: List<RustDependency>,
    procMacroArtifacts: List<String> = emptyList()
): RustModule =
    RustModule(
        crateId = crateId,
        location = location,
        fromWorkspace = true,
        name = crateId.split("/")[0],
        kind = "bin",
        edition = "2018",
        crateFeatures = crateFeatures,
        dependencies = dependencies,
        crateRoot = crateRoot,
        version = "1.2.3",
        procMacroArtifacts = procMacroArtifacts,
    )

fun createTarget(
    packageName: String,
    targetName: String,
    directDependencies: List<String>,
    sources: Set<String>,
    crateRoot: String,
    baseDirectory: String
): Module =
    createModule(
        label = "$packageName:$targetName",
        directDependencies = directDependencies.map { Label(it) },
        rustModule = createRustModule(
            crateId = targetName,
            crateRoot = crateRoot,
            dependencies = directDependencies.map { RustDependency(it, it) },
        ),
        sources = sources.map { URI.create(it) }.toSet(),
        baseDirectory = URI.create(baseDirectory)
    )

fun getSampleModules(): Pair<List<Module>, Map<String, Module>> {
  // B    A
  // | \ / \
  // C  D   E
  // \ /  \ | \
  //  F     G  H

  val pathPrefix = "file:///path/to/targets"

  val moduleA = createTarget(
      packageName = "@//pkgA",
      targetName = "A",
      directDependencies = listOf("@//pkgD:D", "@//pkgE:E"),
      sources = setOf("$pathPrefix/dirA/src/lib.rs"),
      crateRoot = "$pathPrefix/dirA/src/lib.rs",
      baseDirectory = "$pathPrefix/dirA/"
  )
  val moduleB = createTarget(
      packageName = "@//pkgB",
      targetName = "B",
      directDependencies = listOf("@//pkgC:C", "@//pkgD:D"),
      sources = setOf("$pathPrefix/dirB/src/lib.rs"),
      crateRoot = "$pathPrefix/dirB/src/lib.rs",
      baseDirectory = "$pathPrefix/dirB/"
  )
  val moduleC = createTarget(
      packageName = "@//pkgC",
      targetName = "C",
      directDependencies = listOf("@//pkgF:F"),
      sources = setOf("$pathPrefix/dirC/src/lib.rs"),
      crateRoot = "$pathPrefix/dirC/src/lib.rs",
      baseDirectory = "$pathPrefix/dirC/"
  )
  val moduleD = createTarget(
      packageName = "@//pkgD",
      targetName = "D",
      directDependencies = listOf("@//pkgF:F", "@//pkgG:G"),
      sources = setOf("$pathPrefix/dirD/src/lib.rs"),
      crateRoot = "$pathPrefix/dirD/src/lib.rs",
      baseDirectory = "$pathPrefix/dirD/"
  )
  val moduleE = createTarget(
      packageName = "@//pkgE",
      targetName = "E",
      directDependencies = listOf("@//pkgG:G", "@//pkgH:H"),
      sources = setOf("$pathPrefix/dirE/src/lib.rs"),
      crateRoot = "$pathPrefix/dirE/src/lib.rs",
      baseDirectory = "$pathPrefix/dirE/"
  )
  val moduleF = createTarget(
      packageName = "@//pkgF",
      targetName = "F",
      directDependencies = emptyList(),
      sources = setOf("$pathPrefix/dirF/src/lib.rs"),
      crateRoot = "$pathPrefix/dirF/src/lib.rs",
      baseDirectory = "$pathPrefix/dirF/"
  )
  val moduleG = createTarget(
      packageName = "@//pkgG",
      targetName = "G",
      directDependencies = emptyList(),
      sources = setOf("$pathPrefix/dirG/src/lib.rs"),
      crateRoot = "$pathPrefix/dirG/src/lib.rs",
      baseDirectory = "$pathPrefix/dirG/"
  )
  val moduleH = createTarget(
      packageName = "@//pkgH",
      targetName = "H",
      directDependencies = emptyList(),
      sources = setOf("$pathPrefix/dirH/src/lib.rs"),
      crateRoot = "$pathPrefix/dirH/src/lib.rs",
      baseDirectory = "$pathPrefix/dirH/"
  )

  val modules = listOf(moduleA, moduleB, moduleC, moduleD, moduleE, moduleF, moduleG, moduleH)
  val modulesMap = mapOf(
      "A" to moduleA, "B" to moduleB, "C" to moduleC, "D" to moduleD, "E" to moduleE,
      "F" to moduleF, "G" to moduleG, "H" to moduleH
  )

  return Pair(modules, modulesMap)
}