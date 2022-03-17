package org.jetbrains.bsp.bazel.installationcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.net.HostAndPort;
import io.vavr.control.Option;
import java.nio.file.Paths;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextDebuggerAddressEntity;
import org.jetbrains.bsp.bazel.installationcontext.entities.InstallationContextJavaPathEntity;
import org.junit.Test;

public class InstallationContextTest {

  @Test
  public void shouldReturnFailureIfJavaPathIsNotSpecified() {
    // given & when
    var installationContextTry =
        InstallationContext.builder().debuggerAddress(Option.none()).build();

    // then
    assertTrue(installationContextTry.isFailure());
    assertEquals(IllegalStateException.class, installationContextTry.getCause().getClass());
    assertEquals(
        "Installation context creation failed! 'javaPath' has to be defined.",
        installationContextTry.getCause().getMessage());
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
    assertTrue(installationContextTry.isSuccess());
    var installationContext = installationContextTry.get();

    var expectedJavaPath = new InstallationContextJavaPathEntity(Paths.get("/path/to/java"));
    assertEquals(expectedJavaPath, installationContext.getJavaPath());

    var expectedDebuggerAddress =
        Option.of(
            new InstallationContextDebuggerAddressEntity(HostAndPort.fromString("host:8000")));
    assertEquals(expectedDebuggerAddress, installationContext.getDebuggerAddress());
  }
}
