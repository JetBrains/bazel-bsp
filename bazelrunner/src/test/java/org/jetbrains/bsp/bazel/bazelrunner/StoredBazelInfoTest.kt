package org.jetbrains.bsp.bazel.bazelrunner

import org.assertj.core.api.Assertions
import org.jetbrains.bsp.bazel.utils.dope.DopeTemp
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ProjectStorageTest {
  @Test
  fun shouldStoreAndLoadProject() {
    val path = DopeTemp.createTempPath("bazel-info-cache-test.json")

    val storage = BazelInfoStorage(path)

    val empty = storage.load()
    Assertions.assertThat(empty).isNull()

    val bazelInfo = BasicBazelInfo(
        "/private/var/tmp/_bazel/125c7a6ca879ed16a4b4b1a74bc5f27b/execroot/bazel_bsp",
        Paths.get("/Users/user/workspace/bazel-bsp"),
        BazelRelease("6.0.0"))


    storage.store(bazelInfo)
    val loaded = storage.load()
    Assertions.assertThat(loaded).isEqualTo(bazelInfo)
  }
}
