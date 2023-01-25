package org.jetbrains.bsp.bazel.bazelrunner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BazelReleaseTest {
    @Test
    fun oldBazel(){
        val release = BazelRelease("release 4.0.0")
        assertThat(release.major).isEqualTo(4)
        assertThat(release.mainRepositoryReferencePrefix()).isEqualTo("//")
    }

    @Test
    fun newBazel(){
        val release = BazelRelease("release 6.0.0")
        assertThat(release.major).isEqualTo(6)
        assertThat(release.mainRepositoryReferencePrefix()).isEqualTo("@//")
    }

    @Test
    fun newBazelUnofficial(){
        val release = BazelRelease("release 6.0.0-pre20230102")
        assertThat(release.major).isEqualTo(6)
    }

    @Test
    fun newBuazelMutliDigit(){
        val release = BazelRelease("release 16.0.0")
        assertThat(release.major).isEqualTo(16)
    }
}