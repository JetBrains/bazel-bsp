package org.jetbrains.bsp.bazel.server.sync.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.bsp.bazel.commons.Format
import org.jetbrains.bsp.bazel.server.sync.languages.LanguageData
import java.net.URI

data class Module(
    @param:JsonProperty("label") val label: Label,
    // TODO do not build synthetic modules
    @param:JsonProperty("isSynthetic") val isSynthetic: Boolean,
    @param:JsonProperty("directDependencies") val directDependencies: List<Label>,
    @param:JsonProperty("languages") val languages: Set<Language>,
    @param:JsonProperty("tags") val tags: Set<Tag>,
    @param:JsonProperty("baseDirectory") val baseDirectory: URI,
    @param:JsonProperty("sourceSet") val sourceSet: SourceSet,
    @param:JsonProperty("resources") val resources: Set<URI>,
    @param:JsonProperty("sourceDependencies") val sourceDependencies: Set<URI>,
    @param:JsonProperty("languageData") val languageData: LanguageData?,
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
