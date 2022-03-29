package org.jetbrains.bsp.bazel.installationcontext.entities.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class InstallationContextDebuggerAddressEntityMapperTest {

  private InstallationContextDebuggerAddressEntityMapper mapper;

  @BeforeEach
  public void beforeEach() {
    // given
    this.mapper = new InstallationContextDebuggerAddressEntityMapper();
  }

  @Test
  public void
      shouldReturnSuccessWithDebuggerAddressFromProjectViewIfDebuggerAddressIsSpecifiedInProjectView() {
    // given
    var projectView =
        ProjectView.builder()
            .debuggerAddress(
                Option.of(
                    new ProjectViewDebuggerAddressSection(HostAndPort.fromString("host:8000"))))
            .build()
            .get();

    // when
    var debuggerAddressTry = mapper.map(projectView);

    // then
    assertTrue(debuggerAddressTry.isSuccess());
    var debuggerAddressOption = debuggerAddressTry.get();

    assertTrue(debuggerAddressOption.isDefined());
    var debuggerAddress = debuggerAddressOption.get();

    var expectedDebuggerAddress =
        new InstallationContextDebuggerAddressEntity(HostAndPort.fromString("host:8000"));
    assertEquals(expectedDebuggerAddress, debuggerAddress);
  }

  @Test
  public void
      shouldReturnSuccessWithEmptyDebuggerAddressFromProjectViewIfDebuggerAddressIsNotSpecifiedInProjectView() {
    // given
    var projectView = ProjectView.builder().debuggerAddress(Option.none()).build().get();

    // when
    var debuggerAddressTry = mapper.map(projectView);

    // then
    assertTrue(debuggerAddressTry.isSuccess());
    var debuggerAddressOption = debuggerAddressTry.get();

    assertTrue(debuggerAddressOption.isEmpty());
  }
}
