package org.jetbrains.bsp.bazel.server.sync

import org.jetbrains.bsp.bazel.server.sync.model.Project

class ProjectProvider(
    private val projectResolver: ProjectResolver, private val projectStorage: ProjectStorage
) {
    private var project: Project? = null

    @Synchronized
    fun refreshAndGet(): Project =
        loadFromBazel()

    @Synchronized
    fun get(): Project = project ?: loadFromDisk() ?: loadFromBazel()

    private fun loadFromBazel() = projectResolver.resolve().also {
        project = it
        storeOnDisk()
    }

    private fun loadFromDisk() = projectStorage.load()?.also {
        project = it
    }

    private fun storeOnDisk() = projectStorage.store(project)
}
