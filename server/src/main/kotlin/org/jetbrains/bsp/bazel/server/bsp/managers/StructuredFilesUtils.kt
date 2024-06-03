package org.jetbrains.bsp.bazel.server.bsp.managers

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.apache.logging.log4j.Logger
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun String.readXML(log: Logger? = null): Document? = try {
  DocumentBuilderFactory
    .newInstance()
    .newDocumentBuilder()
    .parse(InputSource(StringReader(this)))
} catch (e: Exception) {
  log?.error("Failed to parse string to xml", e)
  null
}

fun String.toJson(log: Logger? = null): JsonElement? = try {
  JsonParser.parseString(this)
} catch (e: Exception) {
  log?.error("Failed to parse string to json", e)
  null
}
