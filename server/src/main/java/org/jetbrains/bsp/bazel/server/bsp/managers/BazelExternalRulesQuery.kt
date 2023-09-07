package org.jetbrains.bsp.bazel.server.bsp.managers

import org.eclipse.lsp4j.jsonrpc.CancelChecker
import org.jetbrains.bsp.bazel.bazelrunner.BazelRunner
import org.w3c.dom.Document
import org.w3c.dom.Node
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
            .executeBazelCommand(parseProcessOutput = false)
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
        return eligibleItems
            .asSequence()
            .mapNotNull { attributes?.getNamedItem("values")?.nodeValue }
            .toList()
    }

    private fun NodeList.asSequence(): Sequence<Node> = object : Sequence<Node> {
        override fun iterator(): Iterator<Node> = object : Iterator<Node> {
            var index = 0
            override fun hasNext() = (index < length)
            override fun next() = item(index++)
        }
    }
}
