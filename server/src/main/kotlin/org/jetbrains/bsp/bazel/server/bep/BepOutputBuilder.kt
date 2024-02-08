package org.jetbrains.bsp.bazel.server.bep

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.NamedSetOfFiles
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.OutputGroup
import org.jetbrains.bsp.bazel.server.paths.BazelPathsResolver
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

data class TextProtoDepSet(val files: Collection<Path>, val children: Collection<String>)

internal class BepOutputBuilder(private val bazelPathsResolver: BazelPathsResolver) {
  private val outputGroups: MutableMap<String, MutableSet<String>> = HashMap()
  private val textProtoFileSets: MutableMap<String, TextProtoDepSet> = HashMap()
  private val rootTargets: MutableSet<String> = HashSet()

  fun storeNamedSet(id: String, namedSetOfFiles: NamedSetOfFiles) {
    val textProtoDepSet = TextProtoDepSet(
      files = namedSetOfFiles
        .filesList
        .filter { it.name.endsWith("bsp-info.textproto") }
        .map { it.toLocalPath() },
      children = namedSetOfFiles.fileSetsList.map { it.id }
    )

    textProtoFileSets[id] = textProtoDepSet
  }

  private fun BuildEventStreamProtos.File.toLocalPath(): Path {
    val mergedPathPrefix = Path(pathPrefixList.joinToString(File.separator))
    val bazelOutputRelativePath = mergedPathPrefix.resolve(name)

    return bazelPathsResolver.resolveOutput(bazelOutputRelativePath)
  }

  fun storeTargetOutputGroups(target: String, outputGroups: List<OutputGroup>) {
    rootTargets.add(target)

    for (group in outputGroups) {
      val fileSets = group.fileSetsList.map { it.id }
      this.outputGroups.computeIfAbsent(group.name) { HashSet() }.addAll(fileSets)
    }
  }

  fun clear() {
    outputGroups.clear()
    textProtoFileSets.clear()
    rootTargets.clear()
  }

  fun build(): BepOutput = BepOutput(outputGroups, textProtoFileSets, rootTargets)
}
