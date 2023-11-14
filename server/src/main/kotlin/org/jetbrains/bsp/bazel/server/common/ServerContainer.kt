package org.jetbrains.bsp.bazel.server.common

import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoStorage
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.bazel.server.sync.MetricsLogger
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspLanguageExtensionsGenerator
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspFallbackAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelExternalRulesQueryImpl
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BazelProjectMapper
import org.jetbrains.bsp.bazel.server.sync.FileProjectStorage
import org.jetbrains.bsp.bazel.server.sync.ProjectProvider
import org.jetbrains.bsp.bazel.server.sync.ProjectResolver
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage
import org.jetbrains.bsp.bazel.server.sync.TargetInfoReader
import org.jetbrains.bsp.bazel.server.sync.TargetKindResolver
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkResolver
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkVersionResolver
import org.jetbrains.bsp.bazel.server.sync.languages.kotlin.KotlinLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.python.PythonLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider

class ServerContainer internal constructor(
  val projectProvider: ProjectProvider,
  val bspClientLogger: BspClientLogger,
  val bspClientTestNotifier: BspClientTestNotifier,
  val bazelInfo: BazelInfo,
  val bazelRunner: BazelRunner,
  val compilationManager: BazelBspCompilationManager,
  val languagePluginsService: LanguagePluginsService
) {
  companion object {
    @JvmStatic
    fun create(
      bspInfo: BspInfo,
      workspaceContextProvider: WorkspaceContextProvider,
      projectStorage: ProjectStorage?,
      bspClientLogger: BspClientLogger,
      bspClientTestNotifier: BspClientTestNotifier,
      bazelRunner: BazelRunner,
      compilationManager: BazelBspCompilationManager,
      metricsLogger: MetricsLogger?
    ): ServerContainer {
      val bazelInfoStorage = BazelInfoStorage(bspInfo)
      val bazelDataResolver =
        BazelInfoResolver(
          bazelRunner,
          bazelInfoStorage
        )
      val bazelInfo = bazelDataResolver.resolveBazelInfo { }

      val aspectsResolver = InternalAspectsResolver(bspInfo, bazelInfo.release)
      val bazelBspLanguageExtensionsGenerator = BazelBspLanguageExtensionsGenerator(aspectsResolver)
      val bazelBspFallbackAspectsManager = BazelBspFallbackAspectsManager(bazelRunner, workspaceContextProvider)
      val bazelBspAspectsManager = BazelBspAspectsManager(
        bazelBspCompilationManager = compilationManager,
        aspectsResolver = aspectsResolver
      )
      val bazelPathsResolver = BazelPathsResolver(bazelInfo)
      val jdkResolver = JdkResolver(bazelPathsResolver, JdkVersionResolver())
      val javaLanguagePlugin = JavaLanguagePlugin(bazelPathsResolver, jdkResolver, bazelInfo)
      val scalaLanguagePlugin = ScalaLanguagePlugin(javaLanguagePlugin, bazelPathsResolver)
      val cppLanguagePlugin = CppLanguagePlugin(bazelPathsResolver)
      val kotlinLanguagePlugin = KotlinLanguagePlugin(javaLanguagePlugin)
      val thriftLanguagePlugin = ThriftLanguagePlugin(bazelPathsResolver)
      val pythonLanguagePlugin = PythonLanguagePlugin(bazelPathsResolver)
      val languagePluginsService = LanguagePluginsService(
        scalaLanguagePlugin, javaLanguagePlugin, cppLanguagePlugin, kotlinLanguagePlugin, thriftLanguagePlugin, pythonLanguagePlugin
      )
      val targetKindResolver = TargetKindResolver()
      val bazelProjectMapper =
        BazelProjectMapper(languagePluginsService, bazelPathsResolver, targetKindResolver, bazelInfo, bspClientLogger, metricsLogger)
      val targetInfoReader = TargetInfoReader()
      val projectResolver = ProjectResolver(
        bazelBspAspectsManager = bazelBspAspectsManager,
        bazelExternalRulesQuery = BazelExternalRulesQueryImpl(bazelRunner, bazelInfo.isBzlModEnabled),
        bazelBspLanguageExtensionsGenerator = bazelBspLanguageExtensionsGenerator,
        bazelBspFallbackAspectsManager = bazelBspFallbackAspectsManager,
        workspaceContextProvider = workspaceContextProvider,
        bazelProjectMapper = bazelProjectMapper,
        bspLogger = bspClientLogger,
        targetInfoReader = targetInfoReader,
        bazelInfo = bazelInfo,
        metricsLogger = metricsLogger
      )
      val finalProjectStorage = projectStorage ?: FileProjectStorage(bspInfo, bspClientLogger)
      val projectProvider = ProjectProvider(projectResolver, finalProjectStorage)
      return ServerContainer(
        projectProvider,
        bspClientLogger,
        bspClientTestNotifier,
        bazelInfo,
        bazelRunner,
        compilationManager,
        languagePluginsService
      )
    }
  }
}
