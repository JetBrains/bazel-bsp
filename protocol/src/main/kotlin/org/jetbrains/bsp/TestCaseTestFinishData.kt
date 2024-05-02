package org.jetbrains.bsp

public data class TestCaseTestFinishData (
        val testCaseName: String,
        val className: String,
        val time: Double,
        val fullError: String?,
        val errorType: String?,
) {
    companion object {
        const val DATA_KIND = "test-case"
    }
}
