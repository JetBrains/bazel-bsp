package org.jetbrains.bsp.bazel.server.sync

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.sync.model.Project
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class FileProjectStorage(private val path: Path) :
    ProjectStorage {

    private val mapper = jacksonObjectMapper()

    constructor(bspInfo: BspInfo) : this(bspInfo.bazelBspDir().resolve("project-cache.json"))

    override fun load(): Project? = path.takeIf(Files::exists)?.let { read() }

    private fun read(): Project = BspClientLogger.timed<Project>(
        "Loading project from local cache"
    ) {
        try {
            return@timed mapper.readValue(path.toFile(), Project::class.java)
        } catch (e: IOException) {
            // TODO figure out why this error is otherwise not propagated to bsp client
            BspClientLogger.error(e.toString())
            throw RuntimeException(e)
        }
    }

    override fun store(project: Project) = BspClientLogger.timed(
        "Saving project to local cache"
    ) {
        try {
            mapper.writeValue(path.toFile(), project)
        } catch (e: IOException) {
            BspClientLogger.error(e.toString())
            throw RuntimeException(e)
        }
    }
}
