package org.jetbrains.bsp.bazel.installationcontext.entities.mappers

import com.google.common.net.HostAndPort
import io.kotest.matchers.shouldBe
import io.vavr.control.Option
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity
import org.jetbrains.bsp.bazel.projectview.model.ProjectView
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InstallationContextDebuggerAddressEntityMapperTest {

    private lateinit var mapper: InstallationContextDebuggerAddressEntityMapper

    @BeforeEach
    fun beforeEach() {
        // given
        this.mapper = InstallationContextDebuggerAddressEntityMapper()
    }

    @Test
    fun `should return success with empty debugger address from project view if debugger address is not specified in project view`() {
        // given
        val projectView = ProjectView.Builder(debuggerAddress = null).build().get()

        // when
        val debuggerAddressTry = mapper.map(projectView)

        // then
        debuggerAddressTry.isSuccess shouldBe true
        val debuggerAddressOption = debuggerAddressTry.get()

        debuggerAddressOption shouldBe Option.none()
    }

    @Test
    fun `should return success with debugger address from project view if debugger address is specified in project view`() {
        // given
        val projectView =
            ProjectView.Builder(
                debuggerAddress = ProjectViewDebuggerAddressSection(HostAndPort.fromString("host:8000"))
            ).build().get()

        // when
        val debuggerAddressTry = mapper.map(projectView)

        // then
        debuggerAddressTry.isSuccess shouldBe true
        val debuggerAddressOption = debuggerAddressTry.get()

        val expectedDebuggerAddress = InstallationContextDebuggerAddressEntity(HostAndPort.fromString("host:8000"))
        debuggerAddressOption shouldBe Option.of(expectedDebuggerAddress)
    }
}
