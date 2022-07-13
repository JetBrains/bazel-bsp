package org.jetbrains.bsp.bazel.server.common

import org.jetbrains.bsp.bazel.bazelrunner.BazelInfo
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoResolver
import org.jetbrains.bsp.bazel.bazelrunner.BazelInfoStorage
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner.Companion.inCwd
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner.Companion.of
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspAspectsManager
import org.jetbrains.bsp.bazel.server.bsp.managers.BazelBspCompilationManager
import org.jetbrains.bsp.bazel.server.bsp.utils.InternalAspectsResolver
import org.jetbrains.bsp.bazel.server.sync.BazelPathsResolver
import org.jetbrains.bsp.bazel.server.sync.BazelProjectMapper
import org.jetbrains.bsp.bazel.server.sync.FileProjectStorage
import org.jetbrains.bsp.bazel.server.sync.ProjectProvider
import org.jetbrains.bsp.bazel.server.sync.ProjectResolver
import org.jetbrains.bsp.bazel.server.sync.ProjectStorage
import org.jetbrains.bsp.bazel.server.sync.TargetKindResolver
import org.jetbrains.bsp.bazel.server.sync.languages.LanguagePluginsService
import org.jetbrains.bsp.bazel.server.sync.languages.cpp.CppLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JavaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkResolver
import org.jetbrains.bsp.bazel.server.sync.languages.java.JdkVersionResolver
import org.jetbrains.bsp.bazel.server.sync.languages.scala.ScalaLanguagePlugin
import org.jetbrains.bsp.bazel.server.sync.languages.thrift.ThriftLanguagePlugin
import org.jetbrains.bsp.bazel.workspacecontext.WorkspaceContextProvider
import java.nio.file.Path

class ServerContainer internal constructor(
    val projectProvider: ProjectProvider,
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
            workspaceRoot: Path?,
            projectStorage: ProjectStorage?
        ): ServerContainer {
            val bazelInfoStorage = BazelInfoStorage(bspInfo)
            val bazelDataResolver = workspaceRoot?.let { root: Path ->
                BazelInfoResolver(of(workspaceContextProvider, root), bazelInfoStorage)
            } ?: BazelInfoResolver(inCwd(workspaceContextProvider), bazelInfoStorage)
            val bazelInfo = bazelDataResolver.resolveBazelInfo()
            val bazelRunner = of(workspaceContextProvider, bazelInfo.workspaceRoot)
            val compilationManager = BazelBspCompilationManager(bazelRunner)
            val aspectsResolver = InternalAspectsResolver(bspInfo)
            val bazelBspAspectsManager = BazelBspAspectsManager(compilationManager, aspectsResolver)
            val bazelPathsResolver = BazelPathsResolver(bazelInfo)
            val jdkResolver = JdkResolver(bazelPathsResolver, JdkVersionResolver())
            val javaLanguagePlugin = JavaLanguagePlugin(bazelPathsResolver, jdkResolver, bazelInfo)
            val scalaLanguagePlugin = ScalaLanguagePlugin(javaLanguagePlugin, bazelPathsResolver)
            val cppLanguagePlugin = CppLanguagePlugin()
            val thriftLanguagePlugin = ThriftLanguagePlugin(bazelPathsResolver)
            val languagePluginsService = LanguagePluginsService(
                scalaLanguagePlugin, javaLanguagePlugin, cppLanguagePlugin, thriftLanguagePlugin
            )
            val targetKindResolver = TargetKindResolver()
            val bazelProjectMapper =
                BazelProjectMapper(languagePluginsService, bazelPathsResolver, targetKindResolver)
            val projectResolver = ProjectResolver(
                bazelBspAspectsManager,
                workspaceContextProvider,
                bazelProjectMapper,
            )
            val finalProjectStorage = projectStorage ?: FileProjectStorage(bspInfo)
            val projectProvider = ProjectProvider(projectResolver, finalProjectStorage)
            return ServerContainer(
                projectProvider,
                bazelInfo,
                bazelRunner,
                compilationManager,
                languagePluginsService
            )
        }
    }
}
