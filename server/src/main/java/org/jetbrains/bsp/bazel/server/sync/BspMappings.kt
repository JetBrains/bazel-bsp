package org.jetbrains.bsp.bazel.server.sync

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.BuildTargetTag
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import io.vavr.collection.HashSet
import io.vavr.collection.Set
import io.vavr.control.Option
import org.jetbrains.bsp.bazel.server.sync.model.Label
import org.jetbrains.bsp.bazel.server.sync.model.Module
import org.jetbrains.bsp.bazel.server.sync.model.Project
import org.jetbrains.bsp.bazel.server.sync.model.Tag
import java.net.URI

object BspMappings {
    fun toBspId(label: Label): BuildTargetIdentifier =
            BuildTargetIdentifier(label.value)

    @JvmStatic
    fun toBspId(module: Module): BuildTargetIdentifier =
            BuildTargetIdentifier(module.label().value)

    @JvmStatic
    fun toBspTag(tag: Tag): Option<String> = when (tag) {
        Tag.APPLICATION -> Option.some(BuildTargetTag.APPLICATION)
        Tag.TEST -> Option.some(BuildTargetTag.TEST)
        Tag.LIBRARY -> Option.some(BuildTargetTag.LIBRARY)
        Tag.NO_IDE -> Option.some(BuildTargetTag.NO_IDE)
        Tag.NO_BUILD -> Option.none()
        Tag.MANUAL -> Option.none()
    }

    fun toBspUri(uri: URI): String = uri.toString()

    @JvmStatic
    fun toBspUri(uri: BuildTargetIdentifier): String = uri.toString()

    @JvmStatic
    fun getModules(project: Project, targets: List<BuildTargetIdentifier>?): Set<Module> =
            toLabels(targets).flatMap { label: Label? -> project.findModule(label) }

    @JvmStatic
    fun toLabels(targets: List<BuildTargetIdentifier>?): Set<Label> =
            HashSet.ofAll(targets).map { obj: BuildTargetIdentifier -> obj.uri }.map { value: String? -> Label.from(value) }

    @JvmStatic
    fun toUri(textDocument: TextDocumentIdentifier): URI = URI.create(textDocument.uri)
}