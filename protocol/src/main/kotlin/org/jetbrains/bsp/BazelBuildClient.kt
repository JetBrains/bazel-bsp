package org.jetbrains.bsp

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.jetbrains.bsp.PublishOutputParams

interface BazelBuildClient {
    @JsonNotification("build/publishOutput")
    fun onBuildPublishOutput(params: PublishOutputParams)
}
