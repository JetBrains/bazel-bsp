package org.jetbrains.bsp.bazel.server.sync

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.server.model.Project

class ProjectProvider(private val projectResolver: ProjectResolver) {
    private var project: Project? = null

    @Synchronized
    fun refreshAndGet(cancelChecker: CancelChecker, build: Boolean): Project =
        loadFromBazel(cancelChecker, build = build)

    @Synchronized
    fun get(cancelChecker: CancelChecker): Project = project ?: loadFromBazel(cancelChecker, false)

    private fun loadFromBazel(cancelChecker: CancelChecker, build: Boolean) = projectResolver.resolve(cancelChecker, build = build).also {
        project = it
        System.gc()
    }
}
