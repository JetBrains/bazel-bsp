package org.jetbrains.bsp.bazel.server.sync

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.server.sync.model.Project

class ProjectProvider(
    private val projectResolver: ProjectResolver, private val projectStorage: ProjectStorage
) {
    private var project: Project? = null

    @Synchronized
    fun refreshAndGet(cancelChecker: CancelChecker, build: Boolean): Project =
        loadFromBazel(cancelChecker, build = build)

    @Synchronized
    fun get(cancelChecker: CancelChecker): Project = project ?: loadFromDisk() ?: loadFromBazel(cancelChecker, false)

    private fun loadFromBazel(cancelChecker: CancelChecker, build: Boolean) = projectResolver.resolve(cancelChecker, build = build).also {
        project = it
        storeOnDisk()
        System.gc()
    }

    private fun loadFromDisk() = projectStorage.load()?.also {
        project = it
    }

    private fun storeOnDisk() = projectStorage.store(project)
}
