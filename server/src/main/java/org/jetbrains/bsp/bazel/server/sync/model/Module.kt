package org.jetbrains.bsp.bazel.server.sync.model

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
    val outputs: Set<URI>,
    val sourceDependencies: Set<URI>,
    val languageData: LanguageData?,
    val environmentVariables: Map<String, String>
)
