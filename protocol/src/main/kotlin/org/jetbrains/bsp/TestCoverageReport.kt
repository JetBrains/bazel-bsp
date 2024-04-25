package org.jetbrains.bsp

public data class TestCoverageReport (
        val lcovReportPath: String
) {
    companion object {
        const val TaskProgressDataKind = "coverage-report"
    }
}
