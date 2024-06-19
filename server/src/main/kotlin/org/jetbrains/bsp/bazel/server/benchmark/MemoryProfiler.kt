package org.jetbrains.bsp.bazel.server.benchmark

import com.sun.management.GarbageCollectionNotificationInfo
import java.lang.Thread.sleep
import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicLong
import javax.management.Notification
import javax.management.NotificationEmitter
import javax.management.NotificationListener
import kotlin.math.max

private const val MB = 1024 * 1024

internal object MemoryProfiler : NotificationListener {
  private val maxUsedMb = AtomicLong()
  private val usedAtExitMb = AtomicLong()

  fun startRecording() {
    createOpenTelemetryMemoryGauges()

    for (bean in ManagementFactory.getGarbageCollectorMXBeans()) {
      (bean as? NotificationEmitter)?.addNotificationListener(this, null, null)
    }
  }

  private fun createOpenTelemetryMemoryGauges() {
    val maxUsedMbGauge = meter.gaugeBuilder("max.used.memory.mb").ofLongs().buildObserver()
    val usedAtExistMbGauge = meter.gaugeBuilder("used.at.exit.mb").ofLongs().buildObserver()
    meter.batchCallback({
      maxUsedMbGauge.record(maxUsedMb.get())
      usedAtExistMbGauge.record(usedAtExitMb.get())
    }, maxUsedMbGauge, usedAtExistMbGauge)
  }

  override fun handleNotification(notification: Notification, handback: Any?) {
    if (notification.type != GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION) return
    val usedMb = getUsedMemoryMb()
    maxUsedMb.getAndUpdate { max(it, usedMb) }
  }

  fun stopRecording() {
    for (bean in ManagementFactory.getGarbageCollectorMXBeans()) {
      (bean as? NotificationEmitter)?.removeNotificationListener(this)
    }
    forceGc()
    val usedAtExitMb = getUsedMemoryMb()
    this.usedAtExitMb.set(usedAtExitMb)
    maxUsedMb.getAndUpdate { max(it, usedAtExitMb) }
  }

  private fun getUsedMemoryMb(): Long {
    val runtime = Runtime.getRuntime()
    return (runtime.totalMemory() - runtime.freeMemory()) / MB
  }

  private fun forceGc() {
    val oldGcCount = getGcCount()
    System.gc()
    while (oldGcCount == getGcCount()) sleep(1)
  }

  private fun getGcCount(): Long = ManagementFactory.getGarbageCollectorMXBeans().mapNotNull {
    it.collectionCount.takeIf { count -> count != -1L }
  }.sum()
}
