package org.jetbrains.bsp.bazel.server.sync

import com.google.protobuf.Message
import com.google.protobuf.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.jetbrains.bsp.bazel.info.BspTargetInfo.CppTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JavaRuntimeInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JvmTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.JavaToolchainInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.KotlinTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.PythonTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.ScalaTargetInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.ScalaToolchainInfo
import org.jetbrains.bsp.bazel.info.BspTargetInfo.TargetInfo
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.FileTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.name
import kotlin.io.path.toPath

class TargetInfoReader {

    // Due to the fact that current IntelliJ-BSP implementation requires restarting the server on each sync,
    // we don't utilize this cache
    data class CachedTargetInfo(val timestamp: FileTime, val bspTargetInfo: TargetInfo)

    private val cache: ConcurrentHashMap<URI, CachedTargetInfo> = ConcurrentHashMap()


    fun readTargetMapFromAspectOutputs(files: Set<URI>): Map<String, TargetInfo> {
        val builderMap = ConcurrentHashMap<String, TargetInfo.Builder>()
        runBlocking(Dispatchers.IO) {
            files.asFlow().collect { addExtensionInfo(it, builderMap) }
        }
        return builderMap.mapValues { (_, builder) -> builder.buildPartial() }.values.groupBy(TargetInfo::getId)
            // If any aspect has already been run on the build graph, it created shadow graph
            // containing new nodes of the same labels as the original ones. In particular,
            // this happens for all protobuf targets, for which a built-in aspect "bazel_java_proto_aspect"
            // is run. In order to correctly address this issue, we would have to provide separate
            // entities (TargetInfos) for each target and each ruleset (or language) instead of just
            // entity-per-label. As long as we don't have it, in case of a conflict we just take the entity
            // that contains JvmTargetInfo as currently it's the most important one for us.
            .mapValues { it.value.find(TargetInfo::hasJvmTargetInfo) ?: it.value.first() }
    }

    private fun addExtensionInfo(uri: URI, builderMap: ConcurrentHashMap<String, TargetInfo.Builder>) {
        val path = uri.toPath()
        val cleanPathName = path.name.substringBefore(".bsp-info.textproto")

        val targetPathPrefix = "${path.parent}/${cleanPathName.substringBeforeLast('.')}"

        val ruleName = cleanPathName.substringAfterLast('.')

        val builder = builderMap.getOrPut(targetPathPrefix) { TargetInfo.newBuilder() }
        addExtensionInfoToTarget(ruleName, uri, builder)
    }

    private fun addExtensionInfoToTarget(
        extensionName: String, uri: URI, targetInfoBuilder: TargetInfo.Builder
    ): TargetInfo.Builder = when (extensionName) {
        "jvm_target_info" -> {
            val builder = readFromFile(uri, JvmTargetInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setJvmTargetInfo(info)
        }

        "scala_target_info" -> {
            val builder = readFromFile(uri, ScalaTargetInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setScalaTargetInfo(info)
        }

        "scala_toolchain_info" -> {
            val builder = readFromFile(uri, ScalaToolchainInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setScalaToolchainInfo(info)
        }

        "kotlin_target_info" -> {
            val builder = readFromFile(uri, KotlinTargetInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setKotlinTargetInfo(info)
        }

        "cpp_target_info" -> {
            val builder = readFromFile(uri, CppTargetInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setCppTargetInfo(info)
        }

        "python_target_info" -> {
            val builder = readFromFile(uri, PythonTargetInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setPythonTargetInfo(info)
        }

        "java_runtime_info" -> {
            val builder: JavaRuntimeInfo.Builder = readFromFile(uri, JavaRuntimeInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setJavaRuntimeInfo(info)
        }

        "java_toolchain_info" -> {
            val builder: JavaToolchainInfo.Builder = readFromFile(uri, JavaToolchainInfo.newBuilder())
            val info = builder.build()
            targetInfoBuilder.setJavaToolchainInfo(info)
        }

        "general" -> {
            val builder: TargetInfo.Builder = readFromFile(uri, TargetInfo.newBuilder())
            val info = builder.buildPartial()
            targetInfoBuilder.mergeFrom(info)
        }

        else -> targetInfoBuilder
    }


    private fun <T : Message.Builder> readFromFile(uri: URI, builder: T): T {
        val parser = TextFormat.Parser.newBuilder().setAllowUnknownFields(true).build()
        parser.merge(Files.newBufferedReader(Paths.get(uri), StandardCharsets.UTF_8), builder)
        return builder
    }

    private fun readTargetInfoFromFile(uri: URI): TargetInfo {
        val builder = TargetInfo.newBuilder()
        val parser = TextFormat.Parser.newBuilder().setAllowUnknownFields(true).build()
        parser.merge(Files.newBufferedReader(Paths.get(uri), StandardCharsets.UTF_8), builder)
        return builder.buildPartial()
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
