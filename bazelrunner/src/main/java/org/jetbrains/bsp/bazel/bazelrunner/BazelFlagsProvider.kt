package org.jetbrains.bsp.bazel.bazelrunner

interface BazelFlagsProvider {
    fun currentBazelFlags(): List<String>
}
