package org.jetbrains.bsp.bazel.server.sync

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.server.sync.model.Project

class ProjectProvider(
    private val projectResolver: ProjectResolver, private val projectStorage: ProjectStorage
) {
    private var project: Project? = null

    @Synchronized
    fun refreshAndGet(cancelChecker: CancelChecker): Project =
        loadFromBazel(cancelChecker)

    @Synchronized
    fun get(cancelChecker: CancelChecker): Project = project ?: loadFromDisk() ?: loadFromBazel(cancelChecker)

    private fun loadFromBazel(cancelChecker: CancelChecker) = projectResolver.resolve(cancelChecker).also {
        project = it
        storeOnDisk()
    }

    private fun loadFromDisk() = projectStorage.load()?.also {
        project = it
    }

    private fun storeOnDisk() = projectStorage.store(project)
}
