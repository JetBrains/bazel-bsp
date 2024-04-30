package org.jetbrains.bsp

public data class TestCoverageReport (
        val lcovReportUri: String
) {
    companion object {
        const val TaskProgressDataKind = "coverage-report"
    }
}
