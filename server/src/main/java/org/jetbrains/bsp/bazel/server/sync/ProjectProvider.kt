package org.jetbrains.bsp.bazel.server.sync

import org.jetbrains.bsp.bazel.server.sync.model.Project

class ProjectProvider(
    private val projectResolver: ProjectResolver, private val projectStorage: ProjectStorage
) {
    private var project: Project? = null

    @Synchronized
    fun refreshAndGet(): Project =
        loadFromBazel(null)

    @Synchronized
    fun get(): Project = project ?: loadFromDisk(null) ?: loadFromBazel(null)

    @Synchronized
    fun get(originId: String?): Project = project ?: loadFromDisk(originId) ?: loadFromBazel(originId)

    private fun loadFromBazel(originId: String?) = projectResolver.resolve(originId).also {
        project = it
        storeOnDisk(originId)
    }

    private fun loadFromDisk(originId: String?) = projectStorage.load(originId)?.also {
        project = it
    }

    private fun storeOnDisk(originId: String?) = projectStorage.store(project, originId)
}
