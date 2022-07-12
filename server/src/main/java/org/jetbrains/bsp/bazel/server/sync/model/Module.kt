package org.jetbrains.bsp.bazel.server.sync.model

import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import java.net.URI

data class Module(
    val label: Label,
    // TODO do not build synthetic modules
    val isSynthetic: Boolean,
    val directDependencies: List<Label>,
    val languages: Set<Language>,
    val tags: Set<Tag>,
    val baseDirectory: URI,
    val sourceSet: SourceSet,
    val resources: Set<URI>,
    val sourceDependencies: Set<URI>,
    val languageData: LanguageData?,
) {

    override fun toString(): String = Format.`object`(
        "Module",
        Format.entry("label", label),
        Format.entry("isSynthetic", isSynthetic),
        Format.entry(
            "directDependencies", Format.iterable(
                directDependencies.stream()
            )
        ),
        Format.entry(
            "languages", Format.iterableShort(
                languages.stream()
            )
        ),
        Format.entry(
            "tags", Format.iterableShort(
                tags.stream()
            )
        ),
        Format.entry("baseDirectory", baseDirectory),
        Format.entry("sourceSet", sourceSet),
        Format.entry(
            "resources", Format.iterableShort(
                resources.stream()
            )
        ),
        Format.entry(
            "sourceDependencies", Format.iterable(
                sourceDependencies.stream()
            )
        ),
        Format.entry("languageData", languageData)
    )
}
