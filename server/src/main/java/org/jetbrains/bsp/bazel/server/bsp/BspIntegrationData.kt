package org.jetbrains.bsp.bazel.server.bsp

import com.jetbrains.bsp.bsp4kt.BuildClient
import io.grpc.Server
import com.jetbrains.jsonrpc4kt.Launcher
import java.io.InputStream
import java.io.PrintStream
import java.io.PrintWriter
import java.util.concurrent.ExecutorService

data class BspIntegrationData(
    val stdout: PrintStream,
    val stdin: InputStream,
    val executor: ExecutorService,
    val traceWriter: PrintWriter?,
) {
    lateinit var launcher: Launcher<BspServerApi, BuildClient>
}
