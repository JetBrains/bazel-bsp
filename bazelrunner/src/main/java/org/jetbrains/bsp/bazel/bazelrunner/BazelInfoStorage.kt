package org.jetbrains.bsp.bazel.bazelrunner

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.logging.log4j.LogManager
import org.jetbrains.bsp.bazel.server.bsp.info.BspInfo
import java.io.IOException
import java.nio.file.Path

class BazelInfoStorage(private val path: Path) {

  constructor(bspInfo: BspInfo) :
      this(bspInfo.bazelBspDir().resolve("bazel-info-cache.json"))

  private val mapper = jacksonObjectMapper()

  fun load(): BazelInfo? =
      try {
        mapper.readValue(path.toFile(), BasicBazelInfo::class.java)
      } catch (e: Exception) {
        LOGGER.debug("Could not load bazel info", e)
        null
      }

  fun store(bazelInfo: BasicBazelInfo): Unit =
      try {
        mapper.writeValue(path.toFile(), bazelInfo)
      } catch (e: IOException) {
        LOGGER.error("Could not store bazel info", e)
      }

  companion object {
    private val LOGGER = LogManager.getLogger(BazelInfoStorage::class.java)
  }
}
