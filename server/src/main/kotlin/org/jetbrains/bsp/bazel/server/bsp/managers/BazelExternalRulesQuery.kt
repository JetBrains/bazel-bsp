package org.jetbrains.bsp.bazel.server.bsp.managers

import org.apache.logging.log4j.LogManager
import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.jetbrains.bsp.bazel.commons.escapeNewLines
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

interface BazelExternalRulesQuery {
  fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String>
}

class BazelExternalRulesQueryImpl(
  private val bazelRunner: BazelRunner,
  private val isBzlModEnabled: Boolean
) : BazelExternalRulesQuery {
  override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> = when (isBzlModEnabled) {
    true -> BazelBzlModExternalRulesQueryImpl(bazelRunner).fetchExternalRuleNames(cancelChecker) +
      BazelWorkspaceExternalRulesQueryImpl(bazelRunner).fetchExternalRuleNames(cancelChecker)

    false -> BazelWorkspaceExternalRulesQueryImpl(bazelRunner).fetchExternalRuleNames(cancelChecker)
  }
}

class BazelWorkspaceExternalRulesQueryImpl(private val bazelRunner: BazelRunner) : BazelExternalRulesQuery {
  override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> =
    bazelRunner.commandBuilder().query()
      .withArgument("//external:*")
      .withFlags(listOf("--output=xml", "--order_output=no"))
      .executeBazelCommand(parseProcessOutput = false)
      .waitAndGetResult(cancelChecker, ensureAllOutputRead = true).let { result ->
        if (result.isNotSuccess) {
          log.warn("Bazel query failed with output: '${result.stderr.escapeNewLines()}'")
          null
        } else result.stdout.readXML(log)?.calculateEligibleRules()
      } ?: listOf()

  private fun Document.calculateEligibleRules(): List<String> {
    val xPath = XPathFactory.newInstance().newXPath()
    val expression =
      "/query/rule[contains(@class, 'http_archive') and " +
        "(not(string[@name='generator_function']) or string[@name='generator_function' and contains(@value, 'http_archive')])" +
        "]//string[@name='name']"
    val eligibleItems = xPath.evaluate(expression, this, XPathConstants.NODESET) as NodeList
    val returnList = mutableListOf<String>()
    for (i in 0 until eligibleItems.length) {
      eligibleItems.item(i).attributes.getNamedItem("value")?.nodeValue?.let { returnList.add(it) }
    }
    return returnList.toList()
  }

  companion object {
    private val log = LogManager.getLogger(BazelExternalRulesQueryImpl::class.java)
  }
}

class BazelBzlModExternalRulesQueryImpl(private val bazelRunner: BazelRunner) : BazelExternalRulesQuery {
  override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> {
    val jsonElement = bazelRunner.commandBuilder().graph()
      .withFlag("--output=json")
      .executeBazelCommand(parseProcessOutput = false)
      .waitAndGetResult(cancelChecker, ensureAllOutputRead = true).let { result ->
        if (result.isNotSuccess) {
          log.warn("Bazel query failed with output: '${result.stderr.escapeNewLines()}'")
          null
        } else result.stdout.toJson(log)
      }
    return jsonElement.extractValuesFromKey("key")
      .map { it.substringBefore('@') } // the element has the format <DEP_NAME>@<DEP_VERSION>
      .distinct()
  }

  companion object {
    private val log = LogManager.getLogger(BazelBzlModExternalRulesQueryImpl::class.java)
  }
}
