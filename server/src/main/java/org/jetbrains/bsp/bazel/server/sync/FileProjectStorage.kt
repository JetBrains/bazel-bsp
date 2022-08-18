package org.jetbrains.bsp.bazel.server.sync

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.sync.model.Project

class FileProjectStorage(private val path: Path, private val logger: BspClientLogger) :
    ProjectStorage {

    private val mapper = jacksonObjectMapper()

    constructor(bspInfo: BspInfo, logger: BspClientLogger) : this(
        bspInfo.bazelBspDir().resolve("project-cache.json"), logger
    )

    override fun load(originId: String?): Project? = path.takeIf(Files::exists)?.let { read(originId) }

    private fun read(originId: String?): Project = logger.timed<Project>(
        "Loading project from local cache", originId
    ) {
        try {
            return@timed mapper.readValue(path.toFile(), Project::class.java)
        } catch (e: IOException) {
            // TODO figure out why this error is otherwise not propagated to bsp client
            logger.error(e.toString(), originId)
            throw RuntimeException(e)
        }
    }

    override fun store(project: Project, originId: String?) = logger.timed(
        "Saving project to local cache", originId
    ) {
        try {
            mapper.writeValue(path.toFile(), project)
        } catch (e: IOException) {
            logger.error(e.toString(), originId)
            throw RuntimeException(e)
        }
    }
}
