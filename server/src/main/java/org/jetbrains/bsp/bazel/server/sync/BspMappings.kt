package org.jetbrains.bsp.bazel.server.sync

import com.jetbrains.bsp.bsp4kt.BuildTargetIdentifier
import com.jetbrains.bsp.bsp4kt.BuildTargetTag
import com.jetbrains.bsp.bsp4kt.TextDocumentIdentifier
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import java.net.URI

object BspMappings {

    fun toBspId(label: Label): BuildTargetIdentifier = BuildTargetIdentifier(label.value)

    fun toBspId(module: Module): BuildTargetIdentifier = BuildTargetIdentifier(module.label.value)

    fun toBspTag(tag: Tag): String? =
        when (tag) {
            Tag.APPLICATION -> BuildTargetTag.Application
            Tag.TEST -> BuildTargetTag.Test
            Tag.LIBRARY -> BuildTargetTag.Library
            Tag.NO_IDE -> BuildTargetTag.NoIde
            Tag.NO_BUILD, Tag.MANUAL -> null
        }

    fun toBspUri(uri: URI): String = uri.toString()

    fun toBspUri(uri: BuildTargetIdentifier): String = uri.uri

    fun getModules(project: Project, targets: List<BuildTargetIdentifier>): Set<Module> =
        toLabels(targets).mapNotNull(project::findModule).toSet()

    fun toUri(textDocument: TextDocumentIdentifier): URI = URI.create(textDocument.uri)

    fun toLabels(targets: List<BuildTargetIdentifier>): Set<Label> =
        targets.map(BuildTargetIdentifier::uri).map(::Label).toSet()
}
