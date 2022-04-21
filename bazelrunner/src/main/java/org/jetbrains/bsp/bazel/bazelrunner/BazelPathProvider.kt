package org.jetbrains.bsp.bazel.bazelrunner

interface BazelPathProvider {
  fun currentBazelPath(): String
}
