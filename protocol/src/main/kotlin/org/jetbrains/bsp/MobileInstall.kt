package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.StatusCode
import com.google.gson.annotations.JsonAdapter
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter

/**
 * See [mobile-install docs](https://bazel.build/docs/user-manual#start)
 */
@JsonAdapter(EnumTypeAdapter.Factory::class)
public enum class MobileInstallStartType(public val value: Int) {
  NO(1),
  COLD(2),
  WARM(3),
  DEBUG(4),
}

public data class MobileInstallParams(
  val target: BuildTargetIdentifier,
  val originId: String,
  val targetDeviceSerialNumber: String,
  val startType: MobileInstallStartType,
)

public data class MobileInstallResult(
  val statusCode: StatusCode,
  var originId: String? = null,
)
