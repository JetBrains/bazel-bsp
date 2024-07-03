package org.jetbrains.bsp.bazel.server

import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.jetbrains.bsp.JoinedBuildClient
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.utils.BazelInfo
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.benchmark.TelemetryConfig
import org.jetbrains.bsp.bazel.server.bsp.BazelBspServerLifetime
import org.jetbrains.bsp.bazel.server.bsp.BazelServices
import org.jetbrains.bsp.bazel.server.bsp.BspIntegrationData
import org.jetbrains.bsp.bazel.server.bsp.BspRequestsRunner
import org.jetbrains.bsp.bazel.server.bsp.BspServerApi
import org.jetbrains.bsp.bazel.server.bsp.TelemetryContextPropagatingLauncherBuilder
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspFallbackAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspLanguageExtensionsGenerator
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.server.model.Label
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.AdditionalAndroidBuildTargetsProvider
import org.jetbrains.bsp.bazel.server.sync.BazelProjectMapper
import org.jetbrains.bsp.bazel.server.sync.BspProjectMapper
import org.jetbrains.bsp.bazel.server.sync.ExecuteService
import org.jetbrains.bsp.bazel.server.sync.ProjectProvider
import org.jetbrains.bsp.bazel.server.sync.ProjectResolver
import org.jetbrains.bsp.bazel.server.sync.ProjectSyncService
import org.jetbrains.bsp.bazel.server.sync.TargetInfoReader
import org.jetbrains.bsp.bazel.server.sync.TargetKindResolver
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.android.AndroidLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.android.KotlinAndroidModulesMerger
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkResolver
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkVersionResolver
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.python.PythonLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.rust.RustLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

class BazelBspServer(
  private val bspInfo: BspInfo,
  private val workspaceContextProvider: WorkspaceContextProvider,
  private val workspaceRoot: Path,
  private val telemetryConfig: TelemetryConfig,
) {
  private val bspState: MutableMap<Label, Set<TextDocumentIdentifier>> = ConcurrentHashMap()

  private fun bspServerData(
    bspClientLogger: BspClientLogger,
    bazelRunner: BazelRunner,
    compilationManager: BazelBspCompilationManager,
    bazelInfo: BazelInfo,
    workspaceContextProvider: WorkspaceContextProvider,
    bazelPathsResolver: BazelPathsResolver,
  ): BazelServices {
    val languagePluginsService = createLanguagePluginsService(bazelPathsResolver)
    val projectProvider = createProjectProvider(
      bspInfo = bspInfo,
      bazelInfo = bazelInfo,
      workspaceContextProvider = workspaceContextProvider,
      bazelRunner = bazelRunner,
      languagePluginsService = languagePluginsService,
      bazelPathsResolver = bazelPathsResolver,
      compilationManager = compilationManager,
      bspClientLogger = bspClientLogger
    )
    val bspProjectMapper = BspProjectMapper(
      languagePluginsService = languagePluginsService,
      workspaceContextProvider = workspaceContextProvider,
      bazelPathsResolver = bazelPathsResolver,
      bazelRunner = bazelRunner,
      bspInfo = bspInfo
    )

    val serverLifetime = BazelBspServerLifetime(workspaceContextProvider)
    val bspRequestsRunner = BspRequestsRunner(serverLifetime)
    val telemetryConfigWithLogger = telemetryConfig.copy(bspClientLogger = bspClientLogger)
    val projectSyncService = ProjectSyncService(bspProjectMapper, projectProvider, telemetryConfigWithLogger)
    val additionalBuildTargetsProvider = AdditionalAndroidBuildTargetsProvider(projectProvider)
    val executeService = ExecuteService(
      compilationManager = compilationManager,
      projectProvider = projectProvider,
      bazelRunner = bazelRunner,
      workspaceContextProvider = workspaceContextProvider,
      bspClientLogger = bspClientLogger,
      bazelPathsResolver = bazelPathsResolver,
      additionalBuildTargetsProvider = additionalBuildTargetsProvider,
      hasAnyProblems = bspState,
    )
    return BazelServices(
      serverLifetime,
      bspRequestsRunner,
      projectSyncService,
      executeService
    )
  }

  private fun createBazelInfo(bazelRunner: BazelRunner): BazelInfo {
    val bazelDataResolver = BazelInfoResolver(bazelRunner)
    return bazelDataResolver.resolveBazelInfo { }
  }

  private fun createLanguagePluginsService(bazelPathsResolver: BazelPathsResolver): LanguagePluginsService {
    val jdkResolver = JdkResolver(bazelPathsResolver, JdkVersionResolver())
    val javaLanguagePlugin = JavaLanguagePlugin(workspaceContextProvider, bazelPathsResolver, jdkResolver)
    val scalaLanguagePlugin = ScalaLanguagePlugin(javaLanguagePlugin, bazelPathsResolver)
    val cppLanguagePlugin = CppLanguagePlugin(bazelPathsResolver)
    val kotlinLanguagePlugin = KotlinLanguagePlugin(javaLanguagePlugin, bazelPathsResolver)
    val thriftLanguagePlugin = ThriftLanguagePlugin(bazelPathsResolver)
    val pythonLanguagePlugin = PythonLanguagePlugin(bazelPathsResolver)
    val rustLanguagePlugin = RustLanguagePlugin(bazelPathsResolver)
    val androidLanguagePlugin = AndroidLanguagePlugin(javaLanguagePlugin, kotlinLanguagePlugin, bazelPathsResolver)

    return LanguagePluginsService(
      scalaLanguagePlugin,
      javaLanguagePlugin,
      cppLanguagePlugin,
      kotlinLanguagePlugin,
      thriftLanguagePlugin,
      pythonLanguagePlugin,
      rustLanguagePlugin,
      androidLanguagePlugin
    )
  }

  private fun createProjectProvider(
    bspInfo: BspInfo,
    bazelInfo: BazelInfo,
    workspaceContextProvider: WorkspaceContextProvider,
    bazelRunner: BazelRunner,
    languagePluginsService: LanguagePluginsService,
    bazelPathsResolver: BazelPathsResolver,
    compilationManager: BazelBspCompilationManager,
    bspClientLogger: BspClientLogger
  ): ProjectProvider {
    val aspectsResolver = InternalAspectsResolver(bspInfo, bazelInfo.release)

    val bazelBspAspectsManager = BazelBspAspectsManager(
      bazelBspCompilationManager = compilationManager,
      aspectsResolver = aspectsResolver
    )
    val bazelBspLanguageExtensionsGenerator = BazelBspLanguageExtensionsGenerator(aspectsResolver, bazelInfo.release)
    val bazelBspFallbackAspectsManager = BazelBspFallbackAspectsManager(bazelRunner, workspaceContextProvider)
    val targetKindResolver = TargetKindResolver()
    val kotlinAndroidModulesMerger = KotlinAndroidModulesMerger()
    val bazelProjectMapper = BazelProjectMapper(
      languagePluginsService,
      bazelPathsResolver,
      targetKindResolver,
      kotlinAndroidModulesMerger,
      bazelInfo,
      bspClientLogger,
    )
    val targetInfoReader = TargetInfoReader()

    val projectResolver = ProjectResolver(
      bazelBspAspectsManager = bazelBspAspectsManager,
      bazelBspLanguageExtensionsGenerator = bazelBspLanguageExtensionsGenerator,
      bazelBspFallbackAspectsManager = bazelBspFallbackAspectsManager,
      workspaceContextProvider = workspaceContextProvider,
      bazelProjectMapper = bazelProjectMapper,
      targetInfoReader = targetInfoReader,
      bazelInfo = bazelInfo,
      bazelRunner = bazelRunner,
      bazelPathsResolver = bazelPathsResolver,
    )
    return ProjectProvider(projectResolver)
  }

  fun buildServer(bspIntegrationData: BspIntegrationData): Launcher<JoinedBuildClient> {
    val bspServerApi = BspServerApi { client: JoinedBuildClient ->
      val bspClientLogger = BspClientLogger(client)
      val bazelRunner = BazelRunner.of(workspaceContextProvider, bspClientLogger, workspaceRoot, bspInfo.bazelBspDir().toString())
      val bazelInfo = createBazelInfo(bazelRunner)
      val bazelPathsResolver = BazelPathsResolver(bazelInfo)
      val compilationManager =
        BazelBspCompilationManager(bazelRunner, bazelPathsResolver, bspState, client, workspaceRoot)
      bspServerData(
        bspClientLogger,
        bazelRunner,
        compilationManager,
        bazelInfo,
        workspaceContextProvider,
        bazelPathsResolver
      )
    }

    val builder = TelemetryContextPropagatingLauncherBuilder<JoinedBuildClient>()
      .setOutput(bspIntegrationData.stdout).setInput(bspIntegrationData.stdin)
      .setLocalService(bspServerApi).setRemoteInterface(JoinedBuildClient::class.java)
      .setExecutorService(bspIntegrationData.executor)
      .let { builder ->
        if (bspIntegrationData.traceWriter != null) {
          builder.traceMessages(bspIntegrationData.traceWriter)
        } else builder
      }

    val launcher = builder.create()

    val client = launcher.remoteProxy
    bspServerApi.init(client)

    return launcher
  }
}
