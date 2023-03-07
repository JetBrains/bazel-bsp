package org.jetbrains.bsp.bazel.server.common

import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoStorage
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.logger.BspClientTestNotifier
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.server.sync.*
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkResolver
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkVersionResolver
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
                compilationManager: BazelBspCompilationManager
        ): ServerContainer {
            val bazelInfoStorage = BazelInfoStorage(bspInfo)
            val bazelDataResolver =
                BazelInfoResolver(
                    bazelRunner,
                    bazelInfoStorage
                )
            val bazelInfo = bazelDataResolver.resolveBazelInfo { }

            val aspectsResolver = InternalAspectsResolver(bspInfo)
            val bazelBspAspectsManager = BazelBspAspectsManager(compilationManager, aspectsResolver)
            val bazelPathsResolver = BazelPathsResolver(bazelInfo)
            val jdkResolver = JdkResolver(bazelPathsResolver, JdkVersionResolver())
            val javaLanguagePlugin = JavaLanguagePlugin(bazelPathsResolver, jdkResolver, bazelInfo)
            val scalaLanguagePlugin = ScalaLanguagePlugin(javaLanguagePlugin, bazelPathsResolver)
            val cppLanguagePlugin = CppLanguagePlugin(bazelPathsResolver)
            val thriftLanguagePlugin = ThriftLanguagePlugin(bazelPathsResolver)
            val languagePluginsService = LanguagePluginsService(
                scalaLanguagePlugin, javaLanguagePlugin, cppLanguagePlugin, thriftLanguagePlugin
            )
            val targetKindResolver = TargetKindResolver()
            val bazelProjectMapper =
                BazelProjectMapper(languagePluginsService, bazelPathsResolver, targetKindResolver, bazelInfo)
            val targetInfoReader = TargetInfoReader()
            val projectResolver = ProjectResolver(
                bazelBspAspectsManager,
                workspaceContextProvider,
                bazelProjectMapper,
                bspClientLogger,
                targetInfoReader,
                bazelInfo
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
