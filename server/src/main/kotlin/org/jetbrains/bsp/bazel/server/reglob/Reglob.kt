package org.jetbrains.bsp.bazel.server.reglob

import com.google.gson.Gson
import java.nio.file.Path

data class Field(
        val text: String?
)

data class Record(
        val fields: List<Field>
)

data class BuildozerResult(
        val records: List<Record>
)
object Reglob {
    fun getGlobs(targetsToImport: Sequence<String>, workspaceRoot: Path): Map<String, String> {
        val sourceEntries = getSourceEntries(targetsToImport, workspaceRoot)
        return sourceEntries
    }

    private fun getSourceEntries(targetsToImport: Sequence<String>, workspaceRoot: Path): Map<String, String> {
        val targetLabels = targetsToImport.toList().toTypedArray()
        val process = Runtime.getRuntime().exec(
                arrayOf("buildozer", "--output_json", "print label srcs") + targetLabels,
                emptyArray<String>(),
                workspaceRoot.toFile())
        process.waitFor()
        val parsed = Gson().fromJson(process.inputStream.reader().readText(), BuildozerResult::class.java)
        return parsed.records
                .filter { it.fields[0].text != null && it.fields[1].text != null }
                .associate { it.fields[0].text!! to it.fields[1].text!! }
    }
}


