package org.jetbrains.bsp.bazel.server.bsp.managers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.apache.logging.log4j.LogManager
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.commons.escapeNewLines
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

interface BazelExternalRulesQuery {
  fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String>
}

class BazelExternalRulesQueryImpl(
  private val bazelRunner: BazelRunner,
  private val isBzlModEnabled: Boolean) : BazelExternalRulesQuery {

  override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> =
    fetchWorkspaceExternalRuleNames(cancelChecker) + fetchBzlModExternalRuleNames(cancelChecker)

  private fun fetchWorkspaceExternalRuleNames(cancelChecker: CancelChecker): List<String> =
    bazelRunner.commandBuilder().query()
      .withArgument("//external:*")
      .withFlags(listOf("--output=xml", "--order_output=no"))
      .executeBazelCommand(parseProcessOutput = false, useBuildFlags = false)
      .waitAndGetResult(cancelChecker, ensureAllOutputRead = true).let { result ->
        if (result.isNotSuccess)
          error("Bazel query failed with output: '${result.stderr.escapeNewLines()}'")
        else
          result.stdout.readXML()?.calculateEligibleRules()
      } ?: listOf()

  private fun String.readXML(): Document? = try {
    DocumentBuilderFactory
      .newInstance()
      .newDocumentBuilder()
      .parse(InputSource(StringReader(this)))
  } catch (e: Exception) {
    log.error("Failed to parse string to xml", e)
    null
  }

  private fun Document.calculateEligibleRules(): List<String> {
    val xPath = XPathFactory.newInstance().newXPath()
    val expression = "/query/rule[not(.//string[@name='generator_function'])]//string[@name='name']"
    val eligibleItems = xPath.evaluate(expression, this, XPathConstants.NODESET) as NodeList
    val returnList = mutableListOf<String>()
    for (i in 0 until eligibleItems.length) {
      eligibleItems.item(i).attributes.getNamedItem("value")?.nodeValue?.let { returnList.add(it) }
    }
    return returnList.toList()
  }

  private fun fetchBzlModExternalRuleNames(cancelChecker: CancelChecker): List<String> =
    if (!isBzlModEnabled) listOf()
    else {
      val jsonElement = bazelRunner.commandBuilder().graph()
        .withFlag("--output=json")
        .executeBazelCommand(parseProcessOutput = false)
        .waitAndGetResult(cancelChecker, ensureAllOutputRead = true).let { result ->
          if (result.isNotSuccess)
            error("Bazel query failed with output: '${result.stderr.escapeNewLines()}'")
          else result.stdout.toJson()
        }
      extractValuesFromKey(jsonElement, "key")
        .map { it.substringBefore('@') } // the element has the format <DEP_NAME>@<DEP_VERSION>
        .distinct()
    }

  private fun String.toJson(): JsonElement? = try {
    Json.parseToJsonElement(this)
  } catch (e: Exception) {
    log.error("Failed to parse string to json", e)
    null
  }

  private fun extractValuesFromKey(jsonElement: JsonElement?, key: String): MutableList<String> {
    val res = mutableListOf<String>()
    extractValuesFromKeyRecursively(jsonElement, key, res)
    return res
  }

  private fun extractValuesFromKeyRecursively(jsonElement: JsonElement?, key: String, res: MutableList<String>) {
    when (jsonElement) {
      is JsonObject -> jsonElement.entries.forEach { (k, v) ->
        if ((k == key) && v is JsonPrimitive && v.contentOrNull != null) {
          res.add(v.content)
        } else {
          extractValuesFromKeyRecursively(v, key, res)
        }
      }

      is JsonArray -> jsonElement.forEach { extractValuesFromKeyRecursively(it, key, res) }
      else -> {}
    }
  }

  companion object {
    private val log = LogManager.getLogger(BazelExternalRulesQueryImpl::class.java)
  }
}
