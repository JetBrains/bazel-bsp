package org.jetbrains.bsp.bazel.server.model

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.BuildTargetTag
import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import java.net.URI

object BspMappings {

    fun toBspId(label: Label): BuildTargetIdentifier = BuildTargetIdentifier(label.value)

    fun toBspId(module: Module): BuildTargetIdentifier = BuildTargetIdentifier(module.label.value)

    fun toBspTag(tag: Tag): String? =
        when (tag) {
            Tag.APPLICATION -> BuildTargetTag.APPLICATION
            Tag.TEST -> BuildTargetTag.TEST
            Tag.LIBRARY -> BuildTargetTag.LIBRARY
            Tag.NO_IDE -> BuildTargetTag.NO_IDE
            Tag.NO_BUILD, Tag.MANUAL -> null
        }

    fun toBspUri(uri: URI): String = uri.toString()

    fun toBspUri(uri: BuildTargetIdentifier): String = uri.uri

    fun toBspUri(module: Module): String = toBspUri(toBspId(module))

    fun getModules(project: Project, targets: List<BuildTargetIdentifier>): Set<Module> =
        toLabels(targets).mapNotNull(project::findModule).toSet()

    fun toUri(textDocument: TextDocumentIdentifier): URI = URI.create(textDocument.uri)

    fun toLabels(targets: List<BuildTargetIdentifier>): Set<Label> =
        targets.map(BuildTargetIdentifier::getUri).map { Label.parse(it) }.toSet()
}
