package org.jetbrains.bsp

public data class TestCaseTestFinishData (
        val time: Double,
        val className: String?,
        val pkg: String?,
        val fullError: String?,
        val errorType: String?,
) {
    companion object {
        const val DATA_KIND = "test-finish-detail"
    }
}
