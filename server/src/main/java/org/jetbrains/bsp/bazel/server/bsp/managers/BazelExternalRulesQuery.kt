package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
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

class BazelExternalRulesQueryImpl(private val bazelRunner: BazelRunner) : BazelExternalRulesQuery {

    override fun fetchExternalRuleNames(cancelChecker: CancelChecker): List<String> =
        bazelRunner.commandBuilder().query()
            .withArgument("//external:*")
            .withFlags(listOf("--output=xml", "--order_output=no"))
            .executeBazelCommand()
            .waitAndGetResult(cancelChecker, ensureAllOutputRead = true)
            .stdout
            .readXML()
            ?.calculateEligibleRules()
            ?: listOf()

    private fun String.readXML(): Document? =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(this)))

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
}
