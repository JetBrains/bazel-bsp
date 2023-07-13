package org.jetbrains.bsp.bazel.server.sync

import com.google.protobuf.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.getLastModifiedTime

class TargetInfoReader {

    data class CachedTargetInfo(val timestamp: FileTime, val bspTargetInfo: TargetInfo)

    private val cache: ConcurrentHashMap<URI, CachedTargetInfo> = ConcurrentHashMap()

    fun readTargetMapFromAspectOutputs(files: Set<URI>): Map<String, TargetInfo> =
        runBlocking(Dispatchers.IO) {
            files.asFlow().map(::getTargetInfo).toList()
                .groupBy (TargetInfo::getId)
                // If any aspect has already been run on the build graph, it created shadow graph
                // containing new nodes of the same labels as the original ones. In particular,
                // this happens for all protobuf targets, for which a built-in aspect "bazel_java_proto_aspect"
                // is run. In order to correctly address this issue, we would have to provide separate
                // entities (TargetInfos) for each target and each ruleset (or language) instead of just
                // entity-per-label. As long as we don't have it, in case of a conflict we just take the entity
                // that contains JavaTargetInfo as currently it's the most important one for us.
                .mapValues { it.value.find{v -> v.hasJavaTargetInfo()} ?: it.value.first() }
        }

    private fun readTargetInfoFromFile(uri: URI): TargetInfo {
        val builder = TargetInfo.newBuilder()
        val parser = TextFormat.Parser.newBuilder().setAllowUnknownFields(true).build()
        parser.merge(Files.newBufferedReader(Paths.get(uri), StandardCharsets.UTF_8), builder)
        return builder.build()
    }

    private fun getTargetInfo(uri: URI): TargetInfo {
        val currentTimestamp = getFileTimeStamp(uri)
        cache[uri]?.let { cached ->
            if (cached.timestamp == currentTimestamp)
                return cached.bspTargetInfo
        }
        val targetInfo = readTargetInfoFromFile(uri)
        cache[uri] = CachedTargetInfo(currentTimestamp, targetInfo)
        return targetInfo
    }

    private fun getFileTimeStamp(uri: URI): FileTime {
        return Paths.get(uri).getLastModifiedTime()
    }
}
