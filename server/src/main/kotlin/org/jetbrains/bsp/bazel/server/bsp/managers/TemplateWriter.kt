package org.jetbrains.bsp.bazel.server.bsp.managers

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.StringWriter
import java.nio.file.Path
import java.util.*
import kotlin.io.path.writeText

class TemplateWriter(private val resourcePath: Path) {
  private val velocityEngine: VelocityEngine = VelocityEngine()

  init {
    velocityEngine.init(properties)
  }

  private val properties: Properties
    get() {
      val props = Properties()
      props["file.resource.loader.path"] = resourcePath.toAbsolutePath().toString()
      props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem")
      return props
    }

  fun writeToFile(templateFilePath: String, outputFile: Path, variableMap: Map<String, String?>) {
    val template = velocityEngine.getTemplate(templateFilePath)
    val context = VelocityContext()
    variableMap.entries.forEach { context.put(it.key, it.value) }
    val writer = StringWriter()
    template.merge(context, writer)
    outputFile.writeText(writer.toString())
  }
}