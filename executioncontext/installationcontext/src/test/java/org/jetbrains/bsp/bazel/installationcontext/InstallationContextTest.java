package org.jetbrains.bsp.bazel.installationcontext;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;
import org.junit.jupiter.api.Test;

public class InstallationContextTest {

  @Test
  public void shouldReturnFailureIfJavaPathIsNotSpecified() {
    // given & when
    var installationContextTry =
        InstallationContext.builder().debuggerAddress(Option.none()).build();

    // then
    assertThat(installationContextTry.isFailure()).isTrue();
    assertThat(installationContextTry.getCause().getClass()).isEqualTo(IllegalStateException.class);
    assertThat(installationContextTry.getCause().getMessage())
        .isEqualTo("Installation context creation failed! 'javaPath' has to be defined.");
  }

  @Test
  public void shouldReturnSuccessIfAllRequiredFieldsAreDefined() {
    // given
    var installationContextTry =
        InstallationContext.builder()
            .javaPath(new InstallationContextJavaPathEntity(Paths.get("/path/to/java")))
            .debuggerAddress(
                Option.of(
                    new InstallationContextDebuggerAddressEntity(
                        HostAndPort.fromString("host:8000"))))
            .build();

    // then
    assertThat(installationContextTry.isSuccess()).isTrue();
    var installationContext = installationContextTry.get();

    var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
    assertThat(installationContext.getJavaPath()).isEqualTo(expectedJavaPath);

    var expectedDebuggerAddress =
        Option.of(
            new InstallationContextDebuggerAddressEntity(HostAndPort.fromString("host:8000")));
    assertThat(installationContext.getDebuggerAddress()).isEqualTo(expectedDebuggerAddress);
  }
}
