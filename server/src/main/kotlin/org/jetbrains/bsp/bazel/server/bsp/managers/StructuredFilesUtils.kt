package org.jetbrains.bsp.bazel.server.bsp.managers

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
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

fun JsonElement?.extractValuesFromKey(key: String): List<String> {
  val res = mutableListOf<String>()
  extractValuesFromKeyRecursively(this, key, res)
  return res
}

private fun extractValuesFromKeyRecursively(jsonElement: JsonElement?, key: String, res: MutableList<String>) {
  when (jsonElement) {
    is JsonObject -> jsonElement.asMap().forEach { (k, v) ->
      if ((k == key) && v is JsonPrimitive && v.isString) {
        res.add(v.asString)
      } else {
        extractValuesFromKeyRecursively(v, key, res)
      }
    }

    is JsonArray -> jsonElement.forEach { extractValuesFromKeyRecursively(it, key, res) }
    else -> {}
  }
}
