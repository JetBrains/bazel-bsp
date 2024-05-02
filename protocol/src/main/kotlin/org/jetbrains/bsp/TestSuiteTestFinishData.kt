package org.jetbrains.bsp

public data class TestSuiteTestFinishData (
        val suiteName: String,
        val pkg: String?,
        val stackTrace: String?,
        val errorType: String?,
) {
    companion object {
        const val DATA_KIND = "test-suite"
    }
}
