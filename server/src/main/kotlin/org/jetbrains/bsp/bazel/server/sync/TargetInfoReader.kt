package org.jetbrains.bsp.bazel.server.sync

import com.google.protobuf.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import org.jetbrains.bsp.bazel.server.model.Label
import java.nio.file.Path
import kotlin.io.path.reader

class TargetInfoReader {
    fun readTargetMapFromAspectOutputs(files: Set<Path>): Map<Label, TargetInfo> {
        return runBlocking(Dispatchers.Default) {
            files.map { file -> async { readFromFile(file) } }.awaitAll()
        }.groupBy { it.id }
            // If any aspect has already been run on the build graph, it created shadow graph
            // containing new nodes of the same labels as the original ones. In particular,
            // this happens for all protobuf targets, for which a built-in aspect "bazel_java_proto_aspect"
            // is run. In order to correctly address this issue, we would have to provide separate
            // entities (TargetInfos) for each target and each ruleset (or language) instead of just
            // entity-per-label. As long as we don't have it, in case of a conflict we just take the entity
            // that contains JvmTargetInfo as currently it's the most important one for us. Later, we sort by
            // shortest size to get a stable result, which should be the default config.
            .mapValues {
                it.value.filter(TargetInfo::hasJvmTargetInfo).minByOrNull { targetInfo -> targetInfo.serializedSize } ?: it.value.first()
            }
            .mapKeys { Label.parse(it.key) }
    }

    private fun readFromFile(file: Path): TargetInfo {
        val builder = TargetInfo.newBuilder()
        val parser = TextFormat.Parser.newBuilder().setAllowUnknownFields(true).build()
        file.reader().use {
            parser.merge(it, builder)
        }
        return builder.build()
    }
}
