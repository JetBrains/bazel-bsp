package org.jetbrains.bsp.bazel.server.bsp

import ch.epfl.scala.bsp4j.BuildClient
import org.eclipse.lsp4j.jsonrpc.Launcher
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.util.concurrent.ExecutorService

data class BspIntegrationData(
    val stdout: OutputStream,
    val stdin: InputStream,
    val executor: ExecutorService,
    val traceWriter: PrintWriter?,
) {
    lateinit var launcher: Launcher<BuildClient>
}
