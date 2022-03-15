package org.jetbrains.bsp.bazel.installationcontext.entities.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.projectview.model.ProjectView;
import org.jetbrains.bsp.bazel.projectview.model.sections.ProjectViewDebuggerAddressSection;
import org.junit.Before;
import org.junit.Test;

public class InstallationContextDebuggerAddressEntityMapperTest {

  private InstallationContextDebuggerAddressEntityMapper mapper;

  @Before
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
    assertThat(debuggerAddressTry.isSuccess()).isTrue();
    var debuggerAddressOption = debuggerAddressTry.get();

    assertThat(debuggerAddressOption.isDefined()).isTrue();
    var debuggerAddress = debuggerAddressOption.get();

    var expectedDebuggerAddress =
        new InstallationContextDebuggerAddressEntity(HostAndPort.fromString("host:8000"));
    assertThat(debuggerAddress).isEqualTo(expectedDebuggerAddress);
  }

  @Test
  public void
      shouldReturnSuccessWithEmptyDebuggerAddressFromProjectViewIfDebuggerAddressIsNotSpecifiedInProjectView() {
    // given
    var projectView = ProjectView.builder().debuggerAddress(Option.none()).build().get();

    // when
    var debuggerAddressTry = mapper.map(projectView);

    // then
    assertThat(debuggerAddressTry.isSuccess()).isTrue();
    var debuggerAddressOption = debuggerAddressTry.get();

    assertThat(debuggerAddressOption.isEmpty()).isTrue();
  }
}
