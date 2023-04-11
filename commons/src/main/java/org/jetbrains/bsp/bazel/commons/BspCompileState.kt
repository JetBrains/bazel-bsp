package org.jetbrains.bsp.bazel.commons;

import ch.epfl.scala.bsp4j.TextDocumentIdentifier
import java.util.concurrent.ConcurrentHashMap

/**
 * @property hasAnyProblems keeps track of problems in given file so BSP reporter
 * can publish diagnostics with an empty array, to clear up former diagnostics.
 * see: https://youtrack.jetbrains.com/issue/BAZEL-376
 */
class BspCompileState {
    val hasAnyProblems = ConcurrentHashMap<String, Set<TextDocumentIdentifier>>()
}
