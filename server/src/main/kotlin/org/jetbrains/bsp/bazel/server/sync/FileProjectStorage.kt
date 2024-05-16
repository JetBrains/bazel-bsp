package org.jetbrains.bsp.bazel.server.sync

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.jetbrains.bsp.bazel.logger.BspClientLogger
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import org.jetbrains.bsp.bazel.server.model.Label
import org.jetbrains.bsp.bazel.server.model.LabelKeyDeserializer
import org.jetbrains.bsp.bazel.server.model.LabelSerializer
import org.jetbrains.bsp.bazel.server.model.Project
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class FileProjectStorage(private val path: Path, private val logger: BspClientLogger) :
    ProjectStorage {
    private val mapper = jacksonMapperBuilder().addModules(
        SimpleModule().apply {
//            addKeySerializer(Label::class.java, LabelSerializer())
//            addKeyDeserializer(Label::class.java, LabelKeyDeserializer())
        }
    ).build()

    constructor(bspInfo: BspInfo, logger: BspClientLogger) : this(
        bspInfo.bazelBspDir().resolve("project-cache.json"), logger
    )

    override fun load(): Project? = null

    private fun read(): Project = logger.timed<Project>(
        "Loading project from local cache"
    ) {
        try {
            return@timed mapper.readValue(path.toFile(), Project::class.java)
        } catch (e: IOException) {
            // TODO https://youtrack.jetbrains.com/issue/BAZEL-620
            logger.error(e.toString())
            throw RuntimeException(e)
        }
    }

    override fun store(project: Project) = logger.timed(
        "Saving project to local cache"
    ) {
    }
}
